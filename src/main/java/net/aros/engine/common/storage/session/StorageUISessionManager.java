package net.aros.engine.common.storage.session;

import net.aros.engine.common.storage.StorageKey;
import net.aros.engine.common.storage.metadata.StorageMetadata;
import net.minecraft.world.entity.player.Player;

public interface StorageUISessionManager {
    void create(Player player);

    void close(Player player);

    void sendMetadata(StorageKey<?> key, StorageMetadata metadata);

    void sendInitialData(InitialData initialData);

    <T> void sendContentUpdate(ContentUpdate<T> update);
}
