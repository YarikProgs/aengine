package net.aros.engine.common.api.value;

import net.aros.engine.server.StorageSavedData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ValueAccess {
    <T> Optional<T> get(String path, ValueType<T> type);

    <T> void set(String path, ValueType<T> type, T value);

    void clear(String path);

    void markDirty();

    @Contract(value = "_ -> new", pure = true)
    static @NotNull ValueAccess wrap(StorageSavedData data) {
        return new ValueAccess() {
            @Override
            public <T> Optional<T> get(String path, ValueType<T> type) {
                return data.get(path, type).map(Value::value);
            }

            @Override
            public <T> void set(String path, ValueType<T> type, T value) {
                data.set(path, type.create(value));
                markDirty();
            }

            @Override
            public void clear(String path) {
                data.clear(path);
            }

            @Override
            public void markDirty() {
                data.setDirty();
            }
        };
    }
}
