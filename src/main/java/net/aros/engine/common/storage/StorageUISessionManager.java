package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public interface StorageUISessionManager {
    void create(Player player);

    void close(Player player);

    void sendMetadata(StorageKey<?> key, StorageMetadata metadata);

    void sendMetadataMap(Map<StorageKey<?>, StorageMetadata> metadataMap);
}
