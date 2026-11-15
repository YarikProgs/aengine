package net.aros.engine.client.dialogue;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.dialogue.DialogueInfo;
import net.aros.engine.common.dialogue.DialoguePhase;
import net.aros.engine.common.dialogue.playing.EndReason;
import net.aros.engine.common.network.c2s.AckDialoguePayload;
import net.aros.engine.common.network.c2s.AckPhrasePayload;
import net.aros.engine.common.network.s2c.DisplayPhrasePayload;
import net.aros.engine.common.network.s2c.StartDialoguePayload;
import net.aros.engine.common.network.s2c.EndDialoguePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Client entry point for dialogue phrases: picks a renderer by dialect and
 * provides the ack sender.
 */
public final class ClientDialogues {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Session current;

    private ClientDialogues() {
    }

    public static void handle(StartDialoguePayload payload) {
        clear();
        PhraseRenderer<?, ?> renderer = PhraseRenderers.get(payload.dialogue().dialect()).orElse(null);
        if (renderer == null) {
            LOGGER.error("No phrase renderer present for '{}'", payload.dialogue().dialect());
            return;
        }
        Session session = new Session(payload.dialogue(), renderer, Minecraft.getInstance().getConnection());
        current = session;
        invoke(() -> renderer.onDialogueStart(session.info)).whenComplete((unused, error) ->
                Minecraft.getInstance().execute(() -> {
                    if (!isCurrent(session) || session.ending) return;
                    session.opening = false;
                    if (error == null) sendLifecycleAck(session, DialoguePhase.START);
                    else LOGGER.error("Failed to open dialogue '{}'", session.info.dialogueId(), error);
                    DisplayPhrasePayload<?, ?> pending = session.phrase;
                    if (pending != null) render(session, pending);
                }));
    }

    public static void handle(DisplayPhrasePayload<?, ?> payload) {
        Session session = current;
        if (session == null || !isCurrent(session) || session.ending
                || !session.info.instanceId().equals(payload.instanceId())) return;
        session.phrase = payload;
        // If opening timed out on the server, retain only the newest phrase until ready.
        if (!session.opening) render(session, payload);
    }

    private static void render(Session session, DisplayPhrasePayload<?, ?> phrase) {
        invoke(() -> renderTyped(session.renderer, phrase)).whenComplete((unused, error) ->
                Minecraft.getInstance().execute(() -> {
                    if (!isCurrent(session) || session.ending || session.phrase != phrase) return;
                    if (error != null) {
                        LOGGER.error("Failed to render dialogue '{}' phrase {}", session.info.dialogueId(), phrase.phraseIndex(), error);
                        return;
                    }
                    session.phrase = null;
                    PacketDistributor.sendToServer(new AckPhrasePayload(session.info.instanceId(), phrase.phraseIndex()));
                }));
    }

    public static void handle(EndDialoguePayload payload) {
        Session session = current;
        if (session == null || !isCurrent(session) || session.ending
                || !session.info.instanceId().equals(payload.dialogue().instanceId())) return;
        session.ending = true;
        session.phrase = null;
        invoke(() -> session.renderer.onDialogueEnd(session.info, payload.reason())).whenComplete((unused, error) ->
                Minecraft.getInstance().execute(() -> {
                    if (!isCurrent(session)) return;
                    if (error == null) sendLifecycleAck(session, DialoguePhase.END);
                    else LOGGER.error("Failed to close dialogue '{}'", session.info.dialogueId(), error);
                    current = null;
                }));
    }

    /** Discards old futures before asking the renderer to clean up locally. */
    public static void clear() {
        Session session = current;
        current = null;
        if (session != null && !session.ending) {
            session.ending = true;
            invoke(() -> session.renderer.onDialogueEnd(session.info, EndReason.CANCELLED))
                    .whenComplete((unused, error) -> {
                        if (error != null) LOGGER.error("Failed to clean up dialogue '{}'", session.info.dialogueId(), error);
                    });
        }
    }

    private static boolean isCurrent(Session session) {
        return current == session && session.connection != null
                && Minecraft.getInstance().getConnection() == session.connection;
    }

    private static void sendLifecycleAck(Session session, DialoguePhase phase) {
        PacketDistributor.sendToServer(new AckDialoguePayload(session.info.instanceId(), phase));
    }

    private static CompletableFuture<Void> invoke(Supplier<CompletableFuture<Void>> callback) {
        try {
            return Objects.requireNonNull(callback.get(), "Renderer returned null future");
        } catch (RuntimeException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    @SuppressWarnings("unchecked") // Renderer and extra info codecs are registered under the same dialect name.
    private static <P, L> CompletableFuture<Void> renderTyped(PhraseRenderer<P, L> renderer, DisplayPhrasePayload<?, ?> phrase) {
        return renderer.render((DisplayPhrasePayload<P, L>) phrase);
    }

    private static final class Session {
        private final DialogueInfo info;
        private final PhraseRenderer<?, ?> renderer;
        private final ClientPacketListener connection;
        private boolean opening = true;
        private boolean ending;
        private DisplayPhrasePayload<?, ?> phrase;

        private Session(DialogueInfo info, PhraseRenderer<?, ?> renderer, ClientPacketListener connection) {
            this.info = info;
            this.renderer = renderer;
            this.connection = connection;
        }
    }
}
