package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.InitialData;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.type.ContentUpdate;
import net.minecraft.world.entity.player.Player;

public interface StorageUISessionManager {
    void create(Player player);

    void close(Player player);

    void sendMetadata(StorageKey<?> key, StorageMetadata metadata);

    void sendInitialData(InitialData initialData);

    <T> void sendContentUpdate(ContentUpdate<T> update);
}
