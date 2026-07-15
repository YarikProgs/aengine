package net.aros.engine.common.storage;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.session.ServerStorageUISession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerStorageUISessionManager implements StorageUISessionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, Session> sessions = new HashMap<>();

    @Override
    public void create(@NotNull Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            LOGGER.error("create requires {}, got {}", ServerPlayer.class.getName(), player.getClass().getName());
            return;
        }
        if (sessions.containsKey(player.getUUID())) {
            LOGGER.warn("Duplicate session for {}, closing older", player);
            close(player);
        }
        sessions.put(player.getUUID(), new Session(serverPlayer));
        collectAndSetMetadataMapTo(player);
    }

    @Override
    public void close(@NotNull Player player) {
        if (!(player instanceof ServerPlayer)) {
            LOGGER.error("close requires {}, got {}", ServerPlayer.class.getName(), player.getClass().getName());
            return;
        }
        if (!sessions.containsKey(player.getUUID())) {
            LOGGER.warn("Can't close session for {}, not present", player);
            return;
        }
        sessions.remove(player.getUUID());
    }

    @Override
    public void sendMetadata(StorageKey<?> key, StorageMetadata metadata) {
        for (Session session : sessions.values()) {
            session.sendMetadata(key, metadata);
        }
    }

    @Override
    public void sendMetadataMap(Map<StorageKey<?>, StorageMetadata> metadataMap) {
        for (Session session : sessions.values()) {
            session.sendMetadataMap(metadataMap);
        }
    }

    private void collectAndSetMetadataMapTo(@NotNull Player player) {
        Map<StorageKey<?>, StorageMetadata> map = new HashMap<>();
        ArosEngine.getEngine().storageManager().getAll().forEach((key, storage) -> {
            if (storage.getMetadata() != null)
                map.put(key, storage.getMetadata());
        });
        sessions.get(player.getUUID()).sendMetadataMap(map);
    }

    private static class Session {
        private final ServerPlayer owner;

        public Session(ServerPlayer owner) {
            this.owner = owner;
        }

        public void sendMetadata(StorageKey<?> key, StorageMetadata metadata) {
            ServerStorageUISession.sendMetadataTo(owner, key, metadata);
        }

        public void sendMetadataMap(Map<StorageKey<?>, StorageMetadata> metadataMap) {
            ServerStorageUISession.sendMetadataMapTo(owner, metadataMap);
        }
    }
}
