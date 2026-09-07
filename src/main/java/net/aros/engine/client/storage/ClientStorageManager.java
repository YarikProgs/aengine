package net.aros.engine.client.storage;

import net.aros.engine.client.events.RegisterClientStorageEvent;
import net.aros.engine.client.storage.session.ClientStorageUISessionManager;
import net.aros.engine.common.storage.AbstractStorage;
import net.aros.engine.common.storage.StorageEntry;
import net.aros.engine.common.storage.StorageKey;
import net.aros.engine.common.storage.metadata.StorageMetadata;
import net.aros.engine.common.storage.session.ContentUpdate;
import net.aros.engine.common.storage.session.StorageUISessionManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.aros.engine.common.ArosEngine.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class ClientStorageManager {
    private static final Map<StorageKey<?>, AbstractStorage<?>> STORAGES = new HashMap<>();
    private static final StorageUISessionManager SESSION_MANAGER = new ClientStorageUISessionManager();

    @SubscribeEvent
    public static void onAddReloadListeners(RegisterClientReloadListenersEvent event) {
        var storages = ModLoader
                .postEventWithReturn(new RegisterClientStorageEvent())
                .getRegisteredStorages();

        STORAGES.clear();
        STORAGES.putAll(storages);
        for (AbstractStorage<?> value : storages.values()) {
            value.addListeners(ClientStorageManager::onMetadataUpdate, ClientStorageManager::onContentUpdate);
            event.registerReloadListener(value.createReloadListener());
        }
    }

    public static void onMetadataUpdate(StorageKey<?> key, StorageMetadata metadata) {
        SESSION_MANAGER.sendMetadata(key, metadata);
    }

    public static <T> void onContentUpdate(StorageKey<T> key, Set<StorageEntry<T>> entries) {
        SESSION_MANAGER.sendContentUpdate(new ContentUpdate<>(key, entries));
    }

    @Nullable
    public static <T> AbstractStorage<T> get(StorageKey<T> key) {
        //noinspection unchecked
        return (AbstractStorage<T>) STORAGES.get(key);
    }

    public static StorageUISessionManager sessionManager() {
        return SESSION_MANAGER;
    }

    public static Map<StorageKey<?>, AbstractStorage<?>> getAll() {
        return Collections.unmodifiableMap(STORAGES);
    }
}
