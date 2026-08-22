package net.aros.engine.common.script;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.script.compilation.backend.ScriptBackendProvider;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.script.backend.ScriptBackendProviders;
import net.aros.engine.common.script.loader.AeScriptLoader;
import net.aros.engine.common.script.storage.AeScriptStorage;
import net.aros.engine.common.systems.CustomStorageSystem;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AeScriptSystem implements CustomStorageSystem {
    public static final StorageKey<AeScript> STORAGE_KEY = new StorageKey<>(ArosEngine.id("scripts"));

    private final List<ScriptBackendProvider> backendProviders;

    public AeScriptSystem() {
        this(ScriptBackendProviders.defaults());
    }

    public AeScriptSystem(Collection<? extends ScriptBackendProvider> backendProviders) {
        this.backendProviders = List.copyOf(Objects.requireNonNull(backendProviders, "backendProviders"));
        if (this.backendProviders.isEmpty()) {
            throw new IllegalArgumentException("At least one script backend provider is required");
        }
    }

    @Override
    public void onStorageRegistration(@NotNull Context ctx) {
        var storage = ctx.registerStorage(new AeScriptStorage(STORAGE_KEY));
        for (ScriptBackendProvider backendProvider : backendProviders) {
            storage.attachLoader(new AeScriptLoader(loaderKey(backendProvider), backendProvider));
        }
    }

    private static ResourceLocation loaderKey(ScriptBackendProvider backendProvider) {
        return ArosEngine.id("scripts/" + backendProvider.language().id());
    }
}
