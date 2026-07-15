package net.aros.engine.client.storage;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.storage.StorageUISessionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientStorageUISessionManager implements StorageUISessionManager {
    @Override
    public void create(Player player) {
        collectAndSetMetadataMap();
    }

    @Override
    public void close(Player player) {
    }

    @Override
    public void sendMetadata(StorageKey<?> key, StorageMetadata metadata) {
        getCurrentStorageUI().ifPresent(ui -> ui.putClientMetadata(key, metadata));
    }

    @Override
    public void sendMetadataMap(Map<StorageKey<?>, StorageMetadata> metadataMap) {
        getCurrentStorageUI().ifPresent(ui -> ui.putAllClientMetadata(metadataMap));
    }

    private void collectAndSetMetadataMap() {
        Map<StorageKey<?>, StorageMetadata> map = new HashMap<>();
        ArosEngineClient.getEngine().storageManager().getAll().forEach((key, storage) -> {
            if (storage.getMetadata() != null)
                map.put(key, storage.getMetadata());
        });
        sendMetadataMap(map);
    }

    private static Optional<StorageUI> getCurrentStorageUI() {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) return Optional.empty();
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageUI storageUI)) return Optional.empty();
        return Optional.of(storageUI);
    }
}
