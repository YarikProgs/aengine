package net.aros.engine.common.api.script.callables;

import net.aros.engine.common.api.script.callables.commandcodecs.CommandCodec;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record PlayerCall(MinecraftServer server, ServerLevel level, ServerPlayer player) implements CallContext {
    public static final CommandCodec<PlayerCall> COMMAND_CODEC = CommandCodec
            .ofPrimitive(EntityArgument.player(), EntityArgument::getPlayer, "player")
            .map(PlayerCall::new);

    public PlayerCall(ServerPlayer player) {
        this(player.server, player.serverLevel(), player);
    }
}
