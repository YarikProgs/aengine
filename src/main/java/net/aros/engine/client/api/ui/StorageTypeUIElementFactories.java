package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.aros.engine.common.api.storage.StorageEntry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StorageTypeUIElementFactories {
    private static final Map<Class<?>, StorageTypeUIElementFactory<?>> FACTORIES = new HashMap<>();
    private static final StorageTypeUIElementFactory<?> DEFAULT_FACTORY = new StorageTypeUIElementFactory.Default();

    public static <T> void register(Class<T> type, StorageTypeUIElementFactory<T> factory) {
        FACTORIES.put(type, factory);
    }

    public static <T> StorageTypeUIElementFactory<T> get(Class<T> type) {
        //noinspection unchecked
        return (StorageTypeUIElementFactory<T>) FACTORIES.getOrDefault(type, DEFAULT_FACTORY);
    }

    public static <T> UIElement create(@NotNull StorageEntry<T> entry) {
        return get(entry.type().type()).create(entry);
    }
}
