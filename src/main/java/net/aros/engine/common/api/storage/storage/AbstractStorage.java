package net.aros.engine.common.api.storage.storage;

import net.aros.engine.common.api.storage.loader.AbstractResourceLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractStorage<T> {
    private final StorageKey<T> key;
    private final Map<ResourceLocation, AbstractResourceLoader<T>> loaders = new HashMap<>();

    protected AbstractStorage(StorageKey<T> key) {
        this.key = key;
    }

    public abstract Optional<T> get(ResourceLocation key);

    public abstract ResourceLocation getKey(T element);

    public abstract boolean contains(ResourceLocation key);

    public abstract Set<StorageEntry<T>> entrySet();

    protected abstract void replaceWith(Map<ResourceLocation, T> contents);

    public StorageKey<T> getKey() {
        return key;
    }

    public final void attachLoader(AbstractResourceLoader<T> loader) {
        loaders.put(loader.key(), loader);
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull ReloadListener createReloadListener() {
        return new ReloadListener();
    }

    @Override
    public String toString() {
        return "Storage[" + key.key() + "]";
    }

    public class ReloadListener implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return key.key();
        }

        @Override
        @ParametersAreNonnullByDefault
        public @NotNull CompletableFuture<Void> reload(
                PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller,
                ProfilerFiller profilerFiller2, Executor executor, Executor executor2
        ) {
            List<CompletableFuture<Map<ResourceLocation, T>>> futures = new ArrayList<>();
            for (AbstractResourceLoader<T> loader : loaders.values()) {
                futures.add(loader.parseAll(resourceManager));
            }

            return CompletableFuture
                    .allOf(futures.toArray(CompletableFuture[]::new))
                    .thenCompose(preparationBarrier::wait)
                    .thenAccept(v -> {
                        Map<ResourceLocation, T> result = new HashMap<>();
                        for (var future : futures) {
                            result.putAll(future.join()); // Already completed
                        }
                        replaceWith(result);
                    });
        }
    }
}
