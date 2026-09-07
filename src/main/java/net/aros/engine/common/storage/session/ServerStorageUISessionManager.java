package net.aros.engine.common.storage.session;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.storage.StorageKey;
import net.aros.engine.common.storage.StorageManager;
import net.aros.engine.common.storage.metadata.StorageMetadata;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

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
        sendInitialDataTo(player);
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
    public void sendInitialData(InitialData initialData) {
        for (Session session : sessions.values()) {
            session.sendInitialData(initialData);
        }
    }

    @Override
    public <T> void sendContentUpdate(ContentUpdate<T> update) {
        for (Session session : sessions.values()) {
            session.sendContentUpdate(update);
        }
    }

    private void sendInitialDataTo(@NotNull Player player) {
        Map<StorageKey<?>, StorageMetadata> metadataMap = new HashMap<>();
        List<ContentUpdate<?>> contentsList = new ArrayList<>();
        StorageManager.getAll().forEach((key, storage) -> {
            if (storage.getMetadata() != null)
                metadataMap.put(key, storage.getMetadata());
            //noinspection unchecked,rawtypes
            contentsList.add(new ContentUpdate<>(key, (Set) storage.entrySet()));
        });
        sessions.get(player.getUUID()).sendInitialData(new InitialData(metadataMap, contentsList));
    }

    private record Session(ServerPlayer owner) {
        public void sendInitialData(InitialData initialData) {
            if (owner.hasPermissions(2)) ServerStorageUISession.sendInitialDataTo(owner, initialData);
        }

        public void sendMetadata(StorageKey<?> key, StorageMetadata metadata) {
            if (owner.hasPermissions(2)) ServerStorageUISession.sendMetadataTo(owner, key, metadata);
        }

        public <T> void sendContentUpdate(ContentUpdate<T> update) {
            if (owner.hasPermissions(2)) ServerStorageUISession.sendContentUpdate(owner, update);
        }
    }
}
