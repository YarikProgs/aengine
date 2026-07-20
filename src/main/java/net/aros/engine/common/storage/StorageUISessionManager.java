package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.InitialData;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.type.ContentUpdate;
import net.minecraft.world.entity.player.Player;

public interface StorageUISessionManager {
    void create(Player player);

    void close(Player player);

    void sendMetadata(StorageKey<?> key, StorageMetadata metadata);

    void sendInitialData(InitialData initialData);

    <T> void sendContentUpdate(ContentUpdate<T> update);
}
