package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.AbstractStorage;
import net.aros.engine.common.api.storage.StorageEntry;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.events.RegisterServerStorageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.aros.engine.common.ArosEngine.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class StorageManager {
    private static final Map<StorageKey<?>, AbstractStorage<?>> STORAGES = new HashMap<>();
    private static final StorageUISessionManager SESSION_MANAGER = new ServerStorageUISessionManager();

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        var storages = NeoForge.EVENT_BUS
                .post(new RegisterServerStorageEvent())
                .getRegisteredStorages();

        STORAGES.clear();
        STORAGES.putAll(storages);
        for (AbstractStorage<?> value : storages.values()) {
            value.addListeners(StorageManager::onMetadataUpdate, StorageManager::onContentUpdate);
            event.addListener(value.createReloadListener());
        }
    }

    public static void onMetadataUpdate(StorageKey<?> key, StorageMetadata metadata) {
        SESSION_MANAGER.sendMetadata(key, metadata);
    }

    public static <T> void onContentUpdate(StorageKey<T> key, Set<StorageEntry<T>> entries) {
        SESSION_MANAGER.sendContentUpdate(new ContentUpdate<>(key, entries));
    }

    public static StorageUISessionManager sessionManager() {
        return SESSION_MANAGER;
    }

    @Nullable
    public static <T> AbstractStorage<T> get(StorageKey<T> key) {
        //noinspection unchecked
        return (AbstractStorage<T>) STORAGES.get(key);
    }

    public static Map<StorageKey<?>, AbstractStorage<?>> getAll() {
        return Collections.unmodifiableMap(STORAGES);
    }
}
