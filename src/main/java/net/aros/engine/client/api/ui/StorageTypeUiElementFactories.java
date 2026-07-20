package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.aros.engine.common.api.storage.storage.StorageEntry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StorageTypeUiElementFactories {
    private static final Map<Class<?>, StorageTypeUiElementFactory<?>> FACTORIES = new HashMap<>();
    private static final StorageTypeUiElementFactory<?> DEFAULT_FACTORY = new StorageTypeUiElementFactory.Default();

    public static <T> void register(Class<T> type, StorageTypeUiElementFactory<T> factory) {
        FACTORIES.put(type, factory);
    }

    public static <T> StorageTypeUiElementFactory<T> get(Class<T> type) {
        //noinspection unchecked
        return (StorageTypeUiElementFactory<T>) FACTORIES.getOrDefault(type, DEFAULT_FACTORY);
    }

    public static <T> UIElement create(@NotNull StorageEntry<T> entry) {
        return get(entry.type().type()).create(entry);
    }
}
