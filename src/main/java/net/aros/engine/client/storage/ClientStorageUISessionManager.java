package net.aros.engine.client.storage;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.api.ClientStorageManager;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.storage.ContentUpdate;
import net.aros.engine.common.storage.InitialData;
import net.aros.engine.common.storage.StorageUISessionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class ClientStorageUISessionManager implements StorageUISessionManager {
    @Override
    public void create(Player player) {
        sendInitialInfo();
    }

    @Override
    public void close(Player player) {
    }

    @Override
    public void sendMetadata(StorageKey<?> key, StorageMetadata metadata) {
        getCurrentStorageUI().ifPresent(ui -> ui.putMetadata(key, metadata, true));
    }

    @Override
    public void sendInitialData(InitialData initialData) {
        getCurrentStorageUI().ifPresent(ui -> {
            ui.putAllMetadata(initialData.metadataMap(), true);
            ui.putAllContents(initialData.contentsList(), true);
        });
    }

    @Override
    public <T> void sendContentUpdate(ContentUpdate<T> update) {
        getCurrentStorageUI().ifPresent(ui -> ui.putContents(update, true));
    }

    private void sendInitialInfo() {
        Map<StorageKey<?>, StorageMetadata> metadataMap = new HashMap<>();
        List<ContentUpdate<?>> contentsList = new ArrayList<>();
        ClientStorageManager.getAll().forEach((key, storage) -> {
            if (storage.getMetadata() != null)
                metadataMap.put(key, storage.getMetadata());
            //noinspection unchecked,rawtypes
            contentsList.add(new ContentUpdate<>(key, (Set) storage.entrySet()));
        });
        sendInitialData(new InitialData(metadataMap, contentsList));
    }

    private static Optional<StorageUI> getCurrentStorageUI() {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) return Optional.empty();
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageUI storageUI)) return Optional.empty();
        return Optional.of(storageUI);
    }
}
