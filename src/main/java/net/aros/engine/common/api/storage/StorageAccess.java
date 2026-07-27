package net.aros.engine.common.api.storage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface StorageAccess {
    <T> AbstractStorage<T> get(StorageKey<T> key);

    @Contract(value = "_ -> new", pure = true)
    static @NotNull StorageAccess wrap(Map<StorageKey<?>, AbstractStorage<?>> map) {
        return new StorageAccess() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> AbstractStorage<T> get(StorageKey<T> key) {
                return (AbstractStorage<T>) map.get(key);
            }
        };
    }
}
