package net.aros.engine.common.api.storage.storage;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class DirectStorage<T> extends AbstractStorage<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceLocation, T> elements = new HashMap<>();

    public DirectStorage(StorageKey<T> key) {
        super(key);
    }

    @Override
    public void put(ResourceLocation key, T element) {
        if (elements.containsKey(key)) {
            // TODO notify player
            LOGGER.error("Re-registration of {} in storage {}", key, getKey());
            return;
        }
        elements.put(key, element);
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
    public boolean contains(ResourceLocation key) {
        return elements.containsKey(key);
    }

    @Override
    public void forEach(BiConsumer<ResourceLocation, T> consumer) {
        elements.forEach(consumer);
    }

    @Override
    protected void replaceWith(Map<ResourceLocation, T> contents) {
        clear();
        elements.putAll(contents);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public Map<ResourceLocation, T> getAsMap() {
        return Collections.unmodifiableMap(elements);
    }
}
