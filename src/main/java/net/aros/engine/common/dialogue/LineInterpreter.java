package net.aros.engine.common.dialogue;

import net.aros.engine.common.dialogue.playing.PlayContext;
import net.aros.haru.runtime.DialogueException;
import net.aros.haru.value.Value;
import net.minecraft.server.level.ServerPlayer;

public interface LineInterpreter {
    void onSingleLine(ServerPlayer viewer, PlayContext ctx, Value[] args) throws DialogueException;

    default void onMultipleLines(ServerPlayer viewer, PlayContext ctx, Value[][] lines) throws DialogueException {
        throw new DialogueException("Dialect '" + ctx.dialogue().dialect().name() + "' does not implement multiple lines");
    }
}
