package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.storage.StorageAccess;
import net.aros.engine.common.api.storage.storage.StorageEntry;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.type.ContentUpdate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StorageManager {
    private final Map<StorageKey<?>, AbstractStorage<?>> storages = new HashMap<>();
    private final StorageAccess storageAccess = StorageAccess.wrap(storages);
    private final StorageUISessionManager sessionManager;

    public StorageManager(StorageUISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void putAll(Map<StorageKey<?>, AbstractStorage<?>> storages) {
        this.storages.putAll(storages);
    }

    public StorageAccess storageAccess() {
        return storageAccess;
    }

    public StorageUISessionManager sessionManager() {
        return sessionManager;
    }

    public void onMetadataUpdate(StorageKey<?> key, StorageMetadata metadata) {
        sessionManager.sendMetadata(key, metadata);
    }

    public <T> void onContentUpdate(StorageKey<T> key, Set<StorageEntry<T>> entries) {
        sessionManager.sendContentUpdate(new ContentUpdate<>(key, entries));
    }

    public Map<StorageKey<?>, AbstractStorage<?>> getAll() {
        return Collections.unmodifiableMap(storages);
    }
}
