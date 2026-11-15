package net.aros.engine.common.dialogue;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * One rendered line with dialect-defined linewise information; Unit when unused.
 */
public record DialogueLine<L>(Component text, L extraInfo) {
    public DialogueLine {
        Objects.requireNonNull(text, "Line text");
        Objects.requireNonNull(extraInfo, "Line extra info");
    }
}
