package net.aros.engine.common.api.script.callables;

import net.aros.engine.common.api.script.callables.commandcodecs.CommandCodec;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public record EntityCall(MinecraftServer server, ServerLevel level, Entity entity) implements CallContext {
    public static final CommandCodec<EntityCall> COMMAND_CODEC = CommandCodec
            .ofPrimitive(EntityArgument.entity(), EntityArgument::getEntity, "entity")
            .map(EntityCall::new);

    public EntityCall(Entity entity) {
        this(entity.getServer(), (ServerLevel) entity.level(), entity);
    }
}
