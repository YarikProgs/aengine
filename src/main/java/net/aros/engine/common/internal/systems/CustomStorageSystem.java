package net.aros.engine.common.internal.systems;

import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.storage.StorageKey;

import java.util.HashMap;
import java.util.Map;

public interface CustomStorageSystem {
    void onStorageRegistration(Context ctx);

    class Context {
        private final Map<StorageKey<?>, AbstractStorage<?>> registeredStorages = new HashMap<>();

        public <T extends AbstractStorage<?>> T registerStorage(T storage) {
            registeredStorages.put(storage.getKey(), storage);
            return storage;
        }

        public Map<StorageKey<?>, AbstractStorage<?>> getRegisteredStorages() {
            return registeredStorages;
        }
    }
}
