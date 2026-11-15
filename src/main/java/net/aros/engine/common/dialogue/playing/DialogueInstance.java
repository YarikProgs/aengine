package net.aros.engine.common.dialogue.playing;

import com.mojang.logging.LogUtils;
import net.aros.canon.core.Can;
import net.aros.canon.core.tx.Sandbox;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.Dialogue;
import net.aros.engine.common.dialogue.DialogueInfo;
import net.aros.engine.common.dialogue.DialoguePhase;
import net.aros.engine.common.dialogue.DialogueLine;
import net.aros.engine.common.network.s2c.DisplayPhrasePayload;
import net.aros.engine.common.network.s2c.EndDialoguePayload;
import net.aros.engine.common.network.s2c.StartDialoguePayload;
import net.aros.haru.runtime.AbstractDialoguePlayer;
import net.aros.haru.runtime.DialogueException;
import net.aros.haru.value.Value;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

/**
 * A single playing dialogue over a fixed audience snapshot. Owns the haru VM
 * and the Canon sandbox. Opening and closing wait for client readiness; pauses
 * hold VM execution at phrase boundaries.
 */
public final class DialogueInstance {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_VM_INSTRUCTIONS = 10_000;

    private final Audience audience;
    private final Dialogue dialogue;
    private final QueuedDialogue origin;
    private final List<UUID> members;
    private final Set<UUID> removed = new HashSet<>();
    private final EnumSet<PauseReason> pauseReasons = EnumSet.noneOf(PauseReason.class);
    private final Map<UUID, Long> disconnectHolds = new HashMap<>();
    private final Sandbox sandbox;
    private final PlayContext context;
    private final AbstractDialoguePlayer player;
    private final DialogueInfo info;
    private final Set<UUID> notified = new HashSet<>();
    private final Set<UUID> lifecyclePending = new HashSet<>();
    private long lifecycleDeadline;

    private State state = State.IDLE;
    private PhraseCycle cycle;
    private Map<ServerPlayer, DisplayPhrasePayload<Object, Object>> bundle;
    private int phraseCounter;
    private EndReason endReason;

    DialogueInstance(Audience audience, QueuedDialogue origin, List<ServerPlayer> members) throws DialogueException {
        this.audience = audience;
        this.dialogue = origin.dialogue();
        this.origin = origin;
        this.members = members.stream().map(ServerPlayer::getUUID).toList();
        this.player = new VmPlayer();
        this.sandbox = Can.sandbox("dialogue/" + dialogue.id());
        this.context = new PlayContext(audience, dialogue, player, sandbox, this);
        this.info = new DialogueInfo(dialogue.id(), dialogue.dialect().name(), UUID.randomUUID());
    }

    /* lifecycle */

    /**
     * Opens client UIs before executing any script instructions. Idempotent.
     */
    void start() {
        if (state != State.IDLE) return;
        state = State.OPENING;
        debug("start", info.instanceId(), dialogue.dialect().name());
        lifecycleDeadline = System.currentTimeMillis() + Math.max(0, ArosEngine.CONFIG.dialogue.startAckTimeoutMs());
        for (ServerPlayer viewer : currentMembers()) {
            if (skipViewer(viewer)) continue;
            notified.add(viewer.getUUID());
            lifecyclePending.add(viewer.getUUID());
            PacketDistributor.sendToPlayer(viewer, new StartDialoguePayload(info));
        }
    }

    /**
     * Server tick: thresholds, held boundaries and disconnect holds.
     */
    void tick(long now) {
        if (state == State.DONE) return;
        if (state == State.CLOSING) {
            if (lifecyclePending.isEmpty() || now >= lifecycleDeadline) {
                if (!lifecyclePending.isEmpty()) debug("timeout", "END", lifecyclePending.size());
                state = State.DONE;
                debug("closed");
            }
            return;
        }
        expireDisconnectHolds(now);
        if (endReason != null) return;

        if (state == State.OPENING) {
            if (!lifecyclePending.isEmpty() && now < lifecycleDeadline) return;
            if (!lifecyclePending.isEmpty()) debug("timeout", "START", lifecyclePending.size());
            debug("ready");
            lifecyclePending.clear();
            if (pauseReasons.isEmpty()) runVm();
            else state = State.HELD;
            return;
        }

        if (state == State.ACKING && cycle != null && cycle.canAdvance(now)) {
            debug("phrase_end", cycle.index(), cycle.pending().size());
            cycle = null;
            if (pauseReasons.isEmpty()) {
                player.resume();
                runVm();
            } else {
                state = State.HELD;
            }
        } else if (state == State.HELD && pauseReasons.isEmpty()) {
            player.resume();
            runVm();
        }
    }

    private void runVm() {
        int budget = MAX_VM_INSTRUCTIONS;
        try {
            while (!player.isDone() && budget-- > 0) {
                player.update();
                if (endReason != null) return;
                if (player.isSuspended()) {
                    if (cycle == null) {
                        LOGGER.warn("Dialogue '{}' dialect '{}' suspended without showing a phrase, resuming",
                                dialogue.id(), dialogue.dialect().name());
                        player.resume();
                    } else {
                        break;
                    }
                }
            }
        } catch (DialogueException | RuntimeException e) {
            LOGGER.error("Dialogue '{}' crashed", dialogue.id(), e);
            debug("error", e.toString());
            finish(EndReason.CRASHED);
            return;
        }

        if (player.isDone()) {
            finish(EndReason.COMPLETED);
        } else if (player.isSuspended() && cycle != null) {
            state = State.ACKING;
        } else {
            LOGGER.error("Dialogue '{}' exceeded the instruction budget, aborting", dialogue.id());
            debug("budget", MAX_VM_INSTRUCTIONS);
            finish(EndReason.CRASHED);
        }
    }

    /* phrase collection (called through PlayContext.show) */
    <P, L> void show(ServerPlayer viewer, List<DialogueLine<L>> lines, P extraInfo) {
        if (endReason != null || bundle == null || lines.isEmpty() || !hasMember(viewer.getUUID())) return;
        var previous = bundle.get(viewer);
        List<DialogueLine<Object>> combined = new ArrayList<>();
        if (previous != null) combined.addAll(previous.lines());
        for (var line : lines) combined.add(new DialogueLine<>(line.text(), line.extraInfo()));
        bundle.put(viewer, new DisplayPhrasePayload<>(dialogue.id(), dialogue.dialect().name(), info.instanceId(),
                phraseCounter + 1, combined, extraInfo));
    }

    /**
     * @return {@code true} if the ack was accepted (fresh phrase, member of audience)
     */
    boolean ack(UUID member, UUID instanceId, int phraseIndex) {
        PhraseCycle current = cycle;
        if (state != State.ACKING || current == null) return false;
        if (!info.instanceId().equals(instanceId) || current.index() != phraseIndex) return false;
        boolean accepted = current.pending().remove(member);
        if (accepted) debug("ack", phraseIndex, member, current.pending().size());
        return accepted;
    }

    boolean ackLifecycle(UUID member, UUID instanceId, DialoguePhase phase) {
        if (!info.instanceId().equals(instanceId)) return false;
        if (phase == DialoguePhase.START && state != State.OPENING
                || phase == DialoguePhase.END && state != State.CLOSING) return false;
        boolean accepted = lifecyclePending.remove(member);
        if (accepted) debug("lifecycle_ack", phase, member, lifecyclePending.size());
        return accepted;
    }

    private void flushBundle() {
        if (endReason != null || bundle.isEmpty()) return;

        long now = System.currentTimeMillis();
        long minMs = 0L;
        Set<UUID> pending = new HashSet<>();
        for (var entry : bundle.entrySet()) {
            ServerPlayer viewer = entry.getKey();
            int chars = 0;
            for (var line : entry.getValue().lines()) chars += line.text().getString().length();
            minMs = Math.max(minMs, readingTimeMs(chars));
            pending.add(viewer.getUUID());
        }

        cycle = new PhraseCycle(++phraseCounter, now + minMs, now + minMs + ArosEngine.CONFIG.dialogue.maxAckWaitMs(), pending);
        debug("phrase", phraseCounter, bundle.size(), minMs);
        bundle.forEach(PacketDistributor::sendToPlayer);
    }

    private boolean skipViewer(ServerPlayer viewer) {
        return viewer.hasDisconnected() || disconnectHolds.containsKey(viewer.getUUID());
    }

    private static long readingTimeMs(int chars) {
        return Math.max(50L, chars * 1000L / Math.max(1, ArosEngine.CONFIG.dialogue.readingSpeedCps()));
    }

    /* pause reasons (hold at phrase boundary, ТЗ п. 8) */

    public boolean pause(PauseReason reason) {
        boolean changed = pauseReasons.add(reason);
        if (changed) debug("pause", reason);
        return changed;
    }

    public boolean resume(PauseReason reason) {
        boolean changed = pauseReasons.remove(reason);
        if (changed) debug("resume", reason);
        return changed;
    }

    /* disconnect holds (ТЗ п. 13): pause first, drop the member after N seconds */

    void onDisconnect(ServerPlayer member, long deadline) {
        UUID uuid = member.getUUID();
        if (hasMember(uuid)) debug("disconnect", uuid);
        lifecyclePending.remove(uuid);
        if (endReason != null) return;
        if (!members.contains(uuid) || removed.contains(uuid)) return;
        if (disconnectHolds.putIfAbsent(uuid, deadline) == null && disconnectHolds.size() == 1) {
            pause(PauseReason.DISCONNECT);
        }
        PhraseCycle current = cycle;
        if (current != null) current.pending().remove(uuid);
    }

    void onReconnect(ServerPlayer member) {
        if (endReason != null || !hasMember(member.getUUID())) return;
        if (disconnectHolds.remove(member.getUUID()) != null) {
            debug("reconnect", member.getUUID());
            if (disconnectHolds.isEmpty()) resume(PauseReason.DISCONNECT);
            // The new client connection has no renderer session from before logout.
            notified.add(member.getUUID());
            if (state == State.OPENING) lifecyclePending.add(member.getUUID());
            PacketDistributor.sendToPlayer(member, new StartDialoguePayload(info));
        }
    }

    private void expireDisconnectHolds(long now) {
        if (disconnectHolds.isEmpty()) return;
        Iterator<Map.Entry<UUID, Long>> iterator = disconnectHolds.entrySet().iterator();
        while (iterator.hasNext()) {
            var hold = iterator.next();
            if (now < hold.getValue()) continue;
            iterator.remove();
            if (disconnectHolds.isEmpty()) resume(PauseReason.DISCONNECT);
            memberLeft(hold.getKey());
            if (endReason != null) return;
        }
    }

    /**
     * Audience composition change (ТЗ п. 9).
     */
    void memberLeft(UUID uuid) {
        if (endReason != null || !hasMember(uuid) || !removed.add(uuid)) return;
        debug("member_left", uuid, dialogue.metadata().onAudienceChange());
        lifecyclePending.remove(uuid);
        PhraseCycle current = cycle;
        if (current != null) current.pending().remove(uuid);

        switch (dialogue.metadata().onAudienceChange()) {
            case CANCEL -> finish(EndReason.CANCELLED);
            case RESTART -> finish(EndReason.AUDIENCE_LOST);
            case CONTINUE -> {
                if (currentMembers().isEmpty()) finish(EndReason.CANCELLED);
            }
        }
    }

    /* termination */

    public void finish(@NotNull EndReason reason) {
        if (endReason != null) return;
        state = State.CLOSING;
        endReason = reason;
        cycle = null;
        lifecyclePending.clear();
        player.terminate();
        boolean transactionSucceeded = false;
        try {
            if (reason == EndReason.COMPLETED) sandbox.commit();
            else sandbox.rollback();
            transactionSucceeded = true;
        } catch (Exception e) {
            LOGGER.error("Failed to {} the sandbox of dialogue '{}'",
                    reason == EndReason.COMPLETED ? "commit" : "rollback", dialogue.id(), e);
            debug("error", e.toString());
        }
        debug("result", reason, reason == EndReason.COMPLETED ? "commit" : "rollback", transactionSucceeded);
        lifecycleDeadline = System.currentTimeMillis() + Math.max(0, ArosEngine.CONFIG.dialogue.endAckTimeoutMs());
        // Include viewers removed during execution so their UI is closed as well.
        var playerList = Can.server().getPlayerList();
        for (UUID uuid : notified) {
            ServerPlayer viewer = playerList.getPlayer(uuid);
            if (viewer == null || viewer.hasDisconnected()) continue;
            lifecyclePending.add(uuid);
            PacketDistributor.sendToPlayer(viewer, new EndDialoguePayload(info, reason));
        }
    }

    /* accessors */

    private void debug(String event, Object... args) {
        // Include removed members until their client UI has been closed.
        for (UUID uuid : members) {
            ServerPlayer viewer = Can.server().getPlayerList().getPlayer(uuid);
            if (viewer != null) DialogueDebug.send(viewer, dialogue.id(), event, args);
        }
    }

    public String debugState() {
        return state + ", phrase=" + phraseCounter + ", pauses=" + pauseReasons
                + ", pending=" + (cycle == null ? lifecyclePending.size() : cycle.pending().size());
    }

    public UUID instanceId() {
        return info.instanceId();
    }

    public Dialogue dialogue() {
        return dialogue;
    }

    public Audience audience() {
        return audience;
    }

    QueuedDialogue origin() {
        return origin;
    }

    public boolean isDone() {
        return state == State.DONE;
    }

    /**
     * Script/transaction has ended, but client closing may still be pending.
     */
    public boolean isEnding() {
        return endReason != null;
    }

    public @Nullable EndReason endReason() {
        return endReason;
    }

    public boolean hasMember(UUID uuid) {
        return members.contains(uuid) && !removed.contains(uuid);
    }

    public boolean isAdmin(ServerPlayer player) {
        return audience.admin().getUUID().equals(player.getUUID());
    }

    public List<ServerPlayer> currentMembers() {
        var playerList = Can.server().getPlayerList();
        return members.stream()
                .filter(uuid -> !removed.contains(uuid))
                .map(playerList::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    /* nested */

    private enum State {
        IDLE, OPENING, ACKING, HELD, CLOSING, DONE
    }

    /**
     * Haru VM callbacks and the context bridge for linked functions.
     */
    private final class VmPlayer extends AbstractDialoguePlayer implements PlayContext.Carrier {
        private VmPlayer() throws DialogueException {
            super(dialogue.linkedProgram(), origin.externals(),
                    dialogue.dialect().textRendererFactory(), dialogue.dialect().fuzzyPolicy());
        }

        @Override
        public void onSingleLine(Value[] values) throws DialogueException {
            bundle = new LinkedHashMap<>();
            try {
                for (ServerPlayer viewer : currentMembers()) {
                    if (skipViewer(viewer)) continue;
                    dialogue.dialect().lineInterpreter().onSingleLine(viewer, context, values);
                }
                flushBundle();
            } finally {
                bundle = null;
            }
        }

        @Override
        public void onMultipleLines(Value[][] lines) throws DialogueException {
            bundle = new LinkedHashMap<>();
            try {
                for (ServerPlayer viewer : currentMembers()) {
                    if (skipViewer(viewer)) continue;
                    dialogue.dialect().lineInterpreter().onMultipleLines(viewer, context, lines);
                }
                flushBundle();
            } finally {
                bundle = null;
            }
        }

        @Override
        public @NotNull PlayContext context() {
            return context;
        }
    }

    private record PhraseCycle(int index, long minDeadline, long maxDeadline, Set<UUID> pending) {
        private boolean canAdvance(long now) {
            return now >= maxDeadline || (pending.isEmpty() && now >= minDeadline);
        }
    }
}
