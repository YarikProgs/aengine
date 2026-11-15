package net.aros.engine.common.dialogue.playing;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.Dialogue;
import net.aros.engine.common.dialogue.Dialogues;
import net.aros.engine.common.dialogue.DialoguePhase;
import net.aros.engine.common.dialogue.OnAudienceChange;
import net.aros.haru.value.Value;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * Dialogue scheduler (ТЗ п. 6): a priority queue per audience, one active
 * dialogue, preemption by priority, FIFO among equals. Ticks instances
 * and handles disconnects.
 */
public final class DialoguesTicker {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<UUID, AudienceState> AUDIENCES = new HashMap<>();

    private DialoguesTicker() {
    }

    public static StartResult start(
            Audience audience,
            ResourceLocation dialogueId,
            Map<String, Value> externals
    ) {
        Dialogue dialogue = Dialogues.get(dialogueId).orElse(null);
        if (dialogue == null) return StartResult.REJECTED;

        AudienceState state = AUDIENCES.computeIfAbsent(audience.id(), id -> new AudienceState(audience));
        QueuedDialogue queued = new QueuedDialogue(dialogue, externals);
        DialogueInstance active = state.active;

        // вытесняем (старый менее приоритетный откат, новый вверх)
        if (active != null && !active.isEnding() && dialogue.metadata().preempts(active.dialogue().metadata())) {
            active.finish(EndReason.PREEMPTED);
            insertFrontOfPriorityClass(state.queue, active.origin());
        }

        insertByPriority(state.queue, queued);
        if (state.active != null) {
            for (ServerPlayer member : liveMembers(audience)) {
                DialogueDebug.send(member, dialogueId, "queued", dialogue.metadata().priority());
            }
        }
        return state.active == null ? tryStartNext(state, queued) : StartResult.QUEUED;
    }

    public static Optional<DialogueInstance> activeFor(ServerPlayer player) {
        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null && !state.active.isEnding() && state.active.hasMember(player.getUUID())) {
                return Optional.of(state.active);
            }
        }
        return Optional.empty();
    }

    /**
     * @return {@code true} if the ack was accepted
     */
    public static boolean ack(ServerPlayer player, UUID instanceId, int phraseIndex) {
        return activeFor(player)
                .map(instance -> instance.ack(player.getUUID(), instanceId, phraseIndex))
                .orElse(false);
    }

    public static boolean ackLifecycle(ServerPlayer player, UUID instanceId, DialoguePhase phase) {
        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null && state.active.ackLifecycle(player.getUUID(), instanceId, phase)) return true;
        }
        return false;
    }

    public static boolean pauseByAdmin(ServerPlayer player) {
        return byAdmin(player, instance -> instance.pause(PauseReason.ADMIN));
    }

    public static boolean resumeByAdmin(ServerPlayer player) {
        return byAdmin(player, instance -> instance.resume(PauseReason.ADMIN));
    }

    public static boolean cancelByAdmin(ServerPlayer player) {
        return byAdmin(player, instance -> instance.finish(EndReason.CANCELLED));
    }

    private static boolean byAdmin(ServerPlayer player, java.util.function.Consumer<DialogueInstance> action) {
        DialogueInstance active = activeFor(player).orElse(null);
        if (active == null || !active.isAdmin(player)) return false;
        action.accept(active);
        return true;
    }

    /* ===== server events ===== */

    public static void tick() {
        if (AUDIENCES.isEmpty()) return;
        long now = System.currentTimeMillis();

        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null) state.active.tick(now);
        }
        for (AudienceState state : List.copyOf(AUDIENCES.values())) {
            DialogueInstance active = state.active;
            if (active != null && active.isDone()) {
                onFinished(state, active);
            }
        }
    }

    public static void onLoggedOut(ServerPlayer player) {
        long deadline = System.currentTimeMillis() + ArosEngine.CONFIG.dialogue.disconnectHoldSeconds() * 1000L;
        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null) state.active.onDisconnect(player, deadline);
        }
    }

    public static void onLoggedIn(ServerPlayer player) {
        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null) state.active.onReconnect(player);
        }
    }

    public static void shutdown() {
        for (AudienceState state : AUDIENCES.values()) {
            if (state.active != null) state.active.finish(EndReason.CANCELLED);
        }
        AUDIENCES.clear();
    }

    private static void onFinished(@NotNull AudienceState state, @NotNull DialogueInstance instance) {
        state.active = null;

        // Рестарт политики: заново для нового состава
        if (instance.endReason() == EndReason.AUDIENCE_LOST
                && instance.dialogue().metadata().onAudienceChange() == OnAudienceChange.RESTART
                && !instance.currentMembers().isEmpty()) {
            state.queue.addFirst(instance.origin());
        }
        tryStartNext(state, null);
    }

    /**
     * Skips entries that cannot be created; reports the requested entry, not a later one.
     * STARTED means opening has begun; VM execution and its errors happen on later ticks.
     * Entries are compared by identity: identical dialogues can be queued more than once.
     */
    private static StartResult tryStartNext(@NotNull AudienceState state, @Nullable QueuedDialogue requested) {
        StartResult result = StartResult.QUEUED;
        while (state.active == null && !state.queue.isEmpty()) {
            QueuedDialogue head = state.queue.removeFirst();
            if (head == requested) result = StartResult.REJECTED;

            List<ServerPlayer> members = liveMembers(state.audience);
            if (members.isEmpty()) {
                continue;
            }

            DialogueInstance instance;
            try {
                instance = new DialogueInstance(state.audience, head, members);
            } catch (Exception e) {
                LOGGER.error("Failed to create an instance of dialogue '{}'", head.dialogue().id(), e);
                for (ServerPlayer member : members) DialogueDebug.send(member, head.dialogue().id(), "error", e.toString());
                continue;
            }
            state.active = instance;
            instance.start();
            return head == requested ? StartResult.STARTED : result;
        }
        if (state.active == null) {
            AUDIENCES.remove(state.audience.id());
        }
        return result;
    }

    private static @NotNull List<ServerPlayer> liveMembers(@NotNull Audience audience) {
        List<ServerPlayer> members = new ArrayList<>();
        for (ServerPlayer player : audience.players()) {
            if (!player.hasDisconnected()) members.add(player);
        }
        return members;
    }

    private static void insertByPriority(@NotNull List<QueuedDialogue> queue, @NotNull QueuedDialogue dialogue) {
        int priority = dialogue.dialogue().metadata().priority();
        int index = 0;
        while (index < queue.size() && queue.get(index).dialogue().metadata().priority() >= priority) {
            index++;
        }
        queue.add(index, dialogue);
    }

    /**
     * Вытесненный возвращается в начало среди равных по приоритету (ТЗ п. 7).
     */
    private static void insertFrontOfPriorityClass(@NotNull List<QueuedDialogue> queue, @NotNull QueuedDialogue dialogue) {
        int priority = dialogue.dialogue().metadata().priority();
        int index = 0;
        while (index < queue.size() && queue.get(index).dialogue().metadata().priority() > priority) {
            index++;
        }
        queue.add(index, dialogue);
    }

    private static final class AudienceState {
        private final Audience audience;
        private final List<QueuedDialogue> queue = new ArrayList<>();
        private DialogueInstance active;

        private AudienceState(Audience audience) {
            this.audience = audience;
        }
    }
}
