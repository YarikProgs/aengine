package net.aros.engine.common.api.storage.event;

import net.aros.engine.common.api.storage.AbstractStorage;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class StorageReloadEvent extends Event {
    private final AbstractStorage<?> storage;

    protected StorageReloadEvent(@NotNull AbstractStorage<?> storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    public @NotNull AbstractStorage<?> getStorage() {
        return storage;
    }

    public static final class Before extends StorageReloadEvent {
        public Before(@NotNull AbstractStorage<?> storage) {
            super(storage);
        }
    }

    public static final class After extends StorageReloadEvent {
        public After(@NotNull AbstractStorage<?> storage) {
            super(storage);
        }
    }
}
