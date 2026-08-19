package net.aros.engine.common.api.script.callables;

import net.aros.engine.common.api.script.callables.commandcodecs.CommandCodec;
import net.minecraft.server.MinecraftServer;

public record GlobalCall(MinecraftServer server) implements CallContext {
    public static final CommandCodec<GlobalCall> COMMAND_CODEC = CommandCodec
            .unit()
            .map(GlobalCall::new);
}
