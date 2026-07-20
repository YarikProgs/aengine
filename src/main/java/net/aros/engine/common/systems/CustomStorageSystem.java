package net.aros.engine.common.systems;

import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.type.StorageType;
import net.aros.engine.common.init.AeRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface CustomStorageSystem {
    void onStorageRegistration(Context ctx);

    class Context {
        private final Map<StorageKey<?>, AbstractStorage<?>> registeredStorages = new HashMap<>();

        public <T extends AbstractStorage<?>> T registerStorage(@NotNull T storage) {
            registeredStorages.put(storage.getKey(), storage);
            return storage;
        }

        public <T> Optional<StorageType<T>> getStorageType(ResourceLocation key, Class<T> type) {
            StorageType<?> got = AeRegistries.STORAGE_TYPE.get(key);
            if (got == null) return Optional.empty();
            if (got.type() != type)
                throw new IllegalStateException("Incompatible storage types: expected " + type + ", got: " + got.type() + " in " + key);
            //noinspection unchecked
            return Optional.of((StorageType<T>) got);
        }

        public <T> StorageType<T> registerStorageType(ResourceLocation key, StorageType<T> type) {
            return Registry.register(AeRegistries.STORAGE_TYPE, key, type);
        }

        public Map<StorageKey<?>, AbstractStorage<?>> getRegisteredStorages() {
            return registeredStorages;
        }
    }
}
