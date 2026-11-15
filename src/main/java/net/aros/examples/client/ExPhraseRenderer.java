package net.aros.examples.client;

import net.aros.engine.client.dialogue.Acks;
import net.aros.engine.client.dialogue.PhraseRenderer;
import net.aros.engine.common.dialogue.DialogueLine;
import net.aros.engine.common.network.s2c.DisplayPhrasePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Displays phrase components in chat without command-based acknowledgement.
 */
public enum ExPhraseRenderer implements PhraseRenderer<Object, Object> {
    INSTANCE;

    @Override
    public CompletableFuture<Void> render(DisplayPhrasePayload<Object, Object> phrase) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
            return Acks.immediate();

        Component msg = phrase.lines().size() == 1
                ? phrase.lines().getFirst().text()
                : ComponentUtils.formatList(phrase.lines().stream().map(DialogueLine::text).toList(), Component.literal(" | "));
        minecraft.player.displayClientMessage(msg, true);

        return Acks.afterReading(msg.getString().length());
    }
}
