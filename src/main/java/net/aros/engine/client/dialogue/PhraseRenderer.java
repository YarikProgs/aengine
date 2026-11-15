package net.aros.engine.client.dialogue;

import net.aros.engine.common.network.s2c.DisplayPhrasePayload;
import net.aros.engine.common.dialogue.DialogueInfo;
import net.aros.engine.common.dialogue.playing.EndReason;

import java.util.concurrent.CompletableFuture;

/**
 * Client-side renderer of dialogue phrases. Renderers are registered per
 * dialect name. P and L must match the dialect's phrasewise and linewise codecs;
 * unused kinds of extra info use Unit.
 */
public interface PhraseRenderer<P, L> {
    /**
     * Completes when the UI is ready for phrases. Called on the client thread;
     * the returned future may complete on any thread. The server wait is bounded.
     */
    default CompletableFuture<Void> onDialogueStart(DialogueInfo dialogue) {
        return Acks.immediate();
    }

    /**
     * @return when the local player has read the phrase — sends
     * the acknowledgement to the server
     */
    CompletableFuture<Void> render(DisplayPhrasePayload<P, L> phrase);

    /**
     * The game transaction is already finished. Completes when the UI is ready for
     * the next dialogue. Called on the client thread; the returned future may
     * complete on any thread. May supersede an unfinished opening/render after a timeout.
     * A new start may likewise supersede this animation; renderers must not let old
     * asynchronous animations overwrite the new UI. Logout uses CANCELLED locally.
     */
    default CompletableFuture<Void> onDialogueEnd(DialogueInfo dialogue, EndReason reason) {
        return Acks.immediate();
    }
}
