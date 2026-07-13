package net.aros.engine.common.storage;

import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.storage.StorageAccess;
import net.aros.engine.common.api.storage.storage.StorageKey;

import java.util.HashMap;
import java.util.Map;

public class StorageManager {
    private final Map<StorageKey<?>, AbstractStorage<?>> storages = new HashMap<>();
    private final StorageAccess storageAccess = StorageAccess.wrap(storages);

    public void putAll(Map<StorageKey<?>, AbstractStorage<?>> storages) {
        this.storages.putAll(storages);
    }

    public StorageAccess storageAccess() {
        return storageAccess;
    }
}
