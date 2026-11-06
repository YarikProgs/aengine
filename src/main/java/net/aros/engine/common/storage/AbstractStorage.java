package net.aros.engine.common.storage;

import net.aros.engine.common.storage.event.StorageReloadEvent;
import net.aros.engine.common.storage.loader.AbstractResourceLoader;
import net.aros.engine.common.storage.metadata.LoaderMetadata;
import net.aros.engine.common.storage.metadata.StorageMetadata;
import net.aros.engine.common.value.ValueType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractStorage<T> {
    private final StorageKey<T> key;
    private final Map<ResourceLocation, AbstractResourceLoader<T>> loaders = new HashMap<>();
    protected final ValueType<T> type;
    private StorageMetadata metadata;
    private Consumer<StorageMetadata> metadataListener;
    private Consumer<Set<StorageEntry<T>>> contentListener;

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

    public final AbstractStorage<T> attachLoader(AbstractResourceLoader<T> loader) {
        loaders.put(loader.key(), loader);
        return this;
    }

    public ReloadListener createReloadListener() {
        return new ReloadListener();
    }

    public void addListeners(
            BiConsumer<StorageKey<T>, StorageMetadata> metadataListener,
            BiConsumer<StorageKey<T>, Set<StorageEntry<T>>> contentListener
    ) {
        this.metadataListener = metadata -> metadataListener.accept(key, metadata);
        this.contentListener = content -> contentListener.accept(key, content);
    }

    private void createAndSendMetadata() {
        Map<ResourceLocation, LoaderMetadata> map = new HashMap<>();
        loaders.forEach((key, loader) -> map.put(key, loader.getMetadata()));
        metadata = new StorageMetadata(map);
        metadataListener.accept(metadata);
    }

    public StorageMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Storage[" + key.key() + "]";
    }

    public class ReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture
                    .supplyAsync(() -> {
                        List<AbstractResourceLoader<T>> activeLoaders = new ArrayList<>(loaders.values());
                        activeLoaders.sort(Comparator.comparingInt(AbstractResourceLoader::loadPriority));
                        for (AbstractResourceLoader<T> loader : activeLoaders) {
                            loader.markReloadStart();
                        }

                        beforeReload();
                        NeoForge.EVENT_BUS.post(new StorageReloadEvent.Before(AbstractStorage.this));
                        createAndSendMetadata();
                        contentListener.accept(entrySet());

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
                        contentListener.accept(entrySet());
                        NeoForge.EVENT_BUS.post(new StorageReloadEvent.After(AbstractStorage.this));
                        afterReload();
                    }, gameExecutor);
        }
    }
}
