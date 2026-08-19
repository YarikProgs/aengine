package net.aros.engine.common.script.storage;

import net.aros.engine.common.api.script.AeScriptApi;
import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.storage.AbstractStorage;
import net.aros.engine.common.api.storage.StorageEntry;
import net.aros.engine.common.api.storage.StorageKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AeScriptStorage extends AbstractStorage<AeScript> {
    private final Map<ResourceLocation, AeScript> scripts = new HashMap<>();

    public AeScriptStorage(StorageKey<AeScript> key) {
        super(key, null);
    }

    @Override
    public Optional<AeScript> get(ResourceLocation key) {
        return Optional.ofNullable(scripts.get(key));
    }

    public Set<ResourceLocation> keys() {
        return Set.copyOf(scripts.keySet());
    }

    public Map<ResourceLocation, AeScript> scripts() {
        return Map.copyOf(scripts);
    }

    @Override
    public ResourceLocation getKey(AeScript element) {
        for (Map.Entry<ResourceLocation, AeScript> entry : scripts.entrySet()) {
            if (entry.getValue() == element) return entry.getKey();
        }
        return null;
    }

    @Override
    public Set<StorageEntry<AeScript>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    protected void replaceWith(Map<ResourceLocation, AeScript> contents) {
        scripts.clear();
        scripts.putAll(contents);
    }

    @Override
    protected void beforeReload() {
        AeScriptApi.clearRegistrations();
    }

    @Override
    protected void afterReload() {
        AeScriptApi.runScripts(scripts());
    }
}
