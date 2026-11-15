package net.aros.engine.common.dialogue.playing;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record SoloAudience(ServerPlayer player) implements Audience {
    @Override
    public UUID id() {
        return player.getUUID();
    }

    @Override
    public ServerPlayer admin() {
        return player;
    }

    @Override
    public Collection<ServerPlayer> players() {
        return List.of(player);
    }
}
