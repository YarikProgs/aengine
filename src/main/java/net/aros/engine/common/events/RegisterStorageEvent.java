package net.aros.engine.common.events;

import net.aros.engine.common.api.storage.AbstractStorage;
import net.aros.engine.common.api.storage.StorageKey;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class RegisterStorageEvent extends Event {
    private final Map<StorageKey<?>, AbstractStorage<?>> registeredStorages = new HashMap<>();

    public <T extends AbstractStorage<?>> T register(@NotNull T storage) {
        registeredStorages.put(storage.getKey(), storage);
        return storage;
    }

    public Map<StorageKey<?>, AbstractStorage<?>> getRegisteredStorages() {
        return registeredStorages;
    }
}
