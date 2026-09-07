package net.aros.engine.common.storage;

import net.aros.engine.common.value.ValueType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class DirectStorage<T> extends AbstractStorage<T> {
    private final Map<ResourceLocation, T> elements = new HashMap<>();

    public DirectStorage(StorageKey<T> key, ValueType<T> type) {
        super(key, type);
    }

    @Override
    public Optional<T> get(ResourceLocation key) {
        return Optional.ofNullable(elements.get(key));
    }

    @Override
    public ResourceLocation getKey(T element) {
        for (var entry : elements.entrySet()) {
            if (entry.getValue() == element)
                return entry.getKey();
        }
        return null;
    }

    @Override
    public Set<StorageEntry<T>> entrySet() {
        Set<StorageEntry<T>> set = new HashSet<>();
        for (Map.Entry<ResourceLocation, T> entry : elements.entrySet()) {
            set.add(new StorageEntry<>(entry.getKey(), type.create(entry.getValue())));
        }
        return set;
    }

    @Override
    protected void replaceWith(Map<ResourceLocation, T> contents) {
        elements.clear();
        elements.putAll(contents);
    }
}
