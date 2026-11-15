package net.aros.engine.common.dialogue.playing;

import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

/**
 * Indivisible group of players a dialogue is shown to. Either a single player
 * or a party. Audience identity ({@link #id()}) must be stable while the group
 * exists; membership is snapshotted when a dialogue starts.
 */
public interface Audience {
    UUID id();

    ServerPlayer admin();

    Collection<ServerPlayer> players();
}
