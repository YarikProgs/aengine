package net.aros.engine.common.storage.storage;

import net.aros.engine.common.storage.loader.AbstractResourceLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public abstract class AbstractStorage<T> {
    private final StorageKey<T> key;
    private final List<AbstractResourceLoader<T>> loaders = new ArrayList<>();
    private final ReloadListener reloadListener = new ReloadListener();

    protected AbstractStorage(StorageKey<T> key) {
        this.key = key;
    }

    public void attachLoader(AbstractResourceLoader<T> loader) {
        loaders.add(loader);
    }

    public final @NotNull List<CompletableFuture<Map<ResourceLocation, T>>> load(ResourceManager manager) {
        List<CompletableFuture<Map<ResourceLocation, T>>> futures = new ArrayList<>();
        for (AbstractResourceLoader<T> loader : loaders) {
            futures.add(loader.parseAll(manager));
        }
        return futures;
    }

    public abstract void put(ResourceLocation key, T element);

    public abstract Optional<T> get(ResourceLocation key);

    public abstract ResourceLocation getKey(T element);

    public abstract boolean contains(ResourceLocation key);

    public abstract void forEach(BiConsumer<ResourceLocation, T> consumer);

    protected abstract void replaceWith(Map<ResourceLocation, T> contents);

    public abstract void clear();

    public StorageKey<T> getKey() {
        return key;
    }

    public ReloadListener getReloadListener() {
        return reloadListener;
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
        public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
            List<CompletableFuture<Map<ResourceLocation, T>>> futures = load(resourceManager);

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
