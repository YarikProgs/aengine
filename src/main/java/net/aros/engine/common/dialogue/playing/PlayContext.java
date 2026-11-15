package net.aros.engine.common.dialogue.playing;

import com.mojang.datafixers.util.Unit;
import net.aros.canon.core.tx.Sandbox;
import net.aros.engine.common.dialogue.Dialogue;
import net.aros.engine.common.dialogue.DialogueLine;
import net.aros.haru.runtime.AbstractDialoguePlayer;
import net.aros.haru.runtime.DialogueException;
import net.aros.haru.value.Value;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Shared context of one playing dialogue, handed to dialect callbacks and
 * linked haru functions. Immutable and viewer-free: line callbacks receive
 * the target player as an argument and pass it back when rendering or
 * showing phrases.
 */
public record PlayContext(Audience audience, Dialogue dialogue, AbstractDialoguePlayer dialoguePlayer,
                          Sandbox sandbox, DialogueInstance instance) {

    /** Sends a phrase to one player. */
    public void show(ServerPlayer player, Component phrase) {
        show(player, phrase, Unit.INSTANCE, Unit.INSTANCE);
    }

    /** Sends several parallel phrases to one player. */
    public void show(ServerPlayer player, List<Component> phrases) {
        show(player, phrases.stream().map(text -> new DialogueLine<>(text, Unit.INSTANCE)).toList(), Unit.INSTANCE);
    }

    /** Shows one line with phrasewise information and Unit linewise information. */
    public <P> void show(ServerPlayer player, Component text, P extraInfo) {
        show(player, text, extraInfo, Unit.INSTANCE);
    }

    /** Shows one line. Extra info values must match the dialect's codecs. */
    public <P, L> void show(ServerPlayer player, Component text, P extraInfo, L lineExtraInfo) {
        show(player, List.of(new DialogueLine<>(text, lineExtraInfo)), extraInfo);
    }

    /**
     * Appends parallel lines for this viewer. Extra info applies to the entire phrase;
     * if called repeatedly within one callback, the last phrasewise value wins.
     * Text-only overloads use Unit.
     * No metadata carries over to the next phrase. Unused scopes use Unit codecs;
     * custom codecs require matching extra info values on every phrase/line.
     */
    public <P, L> void show(ServerPlayer player, List<DialogueLine<L>> lines, P extraInfo) {
        instance.show(player, lines, extraInfo);
    }

    /**
     * Renders a haru value into a plain component for the given player:
     * translation and text substitutions are applied by the haru renderer.
     */
    public Component render(ServerPlayer player, Value value) throws DialogueException {
        return Component.literal(dialoguePlayer.renderValue(value, player.getLanguage()));
    }

    public boolean isAdmin(ServerPlayer player) {
        return instance.isAdmin(player);
    }

    public boolean hasMember(UUID uuid) {
        return instance.hasMember(uuid);
    }

    /**
     * Implemented by the dialogue VM player so linked haru functions can
     * access the context: {@code ((Carrier) player).context()}.
     */
    public interface Carrier {
        @NotNull PlayContext context();
    }
}
