package net.aros.engine.common.api.storage;

import net.aros.engine.common.api.storage.loader.AbstractResourceLoader;
import net.aros.engine.common.api.storage.event.StorageReloadEvent;
import net.aros.engine.common.api.storage.metadata.LoaderMetadata;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.value.ValueType;
import net.aros.engine.common.storage.StorageManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractStorage<T> {
    private final StorageKey<T> key;
    private final Map<ResourceLocation, AbstractResourceLoader<T>> loaders = new HashMap<>();
    protected final ValueType<T> type;
    private StorageManager storageManager;
    private StorageMetadata metadata;

    protected AbstractStorage(StorageKey<T> key, ValueType<T> type) {
        this.key = key;
        this.type = type;
    }

    public abstract Optional<T> get(ResourceLocation key);

    public abstract ResourceLocation getKey(T element);

    public abstract Set<StorageEntry<T>> entrySet();

    protected abstract void replaceWith(Map<ResourceLocation, T> contents);

    protected void beforeReload() {
    }

    protected void afterReload() {
    }

    public StorageKey<T> getKey() {
        return key;
    }

    public final void attachLoader(AbstractResourceLoader<T> loader) {
        loaders.put(loader.key(), loader);
    }

    public @NotNull ReloadListener createReloadListener() {
        return new ReloadListener();
    }

    public void setManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    private void createAndSendMetadata() {
        Map<ResourceLocation, LoaderMetadata> map = new HashMap<>();
        loaders.forEach((key, loader) -> map.put(key, loader.getMetadata()));
        metadata = new StorageMetadata(map);
        storageManager.onMetadataUpdate(key, metadata);
    }

    private void sendContent() {
        storageManager.onContentUpdate(key, entrySet());
    }

    public StorageMetadata getMetadata() {
        return metadata;
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
                PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor
        ) {
            return CompletableFuture
                    .supplyAsync(() -> {
                        List<AbstractResourceLoader<T>> activeLoaders = List.copyOf(loaders.values());
                        for (AbstractResourceLoader<T> loader : activeLoaders) {
                            loader.markReloadStart();
                        }

                        beforeReload();
                        NeoForge.EVENT_BUS.post(new StorageReloadEvent.Before(AbstractStorage.this));
                        createAndSendMetadata();
                        sendContent();

                        return activeLoaders;
                    }, gameExecutor)
                    .thenCompose(activeLoaders -> {
                        List<CompletableFuture<Map<ResourceLocation, T>>> futures = new ArrayList<>();
                        for (AbstractResourceLoader<T> loader : activeLoaders) {
                            futures.add(CompletableFuture.supplyAsync(
                                    () -> loader.parseAll(resourceManager),
                                    backgroundExecutor
                            ));
                        }

                        return CompletableFuture
                                .allOf(futures.toArray(CompletableFuture[]::new))
                                .thenApplyAsync(v -> {
                                    Map<ResourceLocation, T> result = new HashMap<>();
                                    for (var future : futures) {
                                        result.putAll(future.join()); // Already completed
                                    }
                                    return result;
                                }, backgroundExecutor);
                    })
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(result -> {
                        replaceWith(result);
                        createAndSendMetadata();
                        sendContent();
                        NeoForge.EVENT_BUS.post(new StorageReloadEvent.After(AbstractStorage.this));
                        afterReload();
                    }, gameExecutor);
        }
    }
}
