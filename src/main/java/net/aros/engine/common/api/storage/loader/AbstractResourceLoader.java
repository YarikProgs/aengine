package net.aros.engine.common.api.storage.loader;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.aros.engine.common.api.storage.storage.metadata.LoadStatus;
import net.aros.engine.common.api.storage.storage.metadata.LoaderMetadata;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractResourceLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String path;
    private final String fileExtension;
    private final ResourceLocation key;
    private KeyFunction<T> keyFactory;
    private volatile LoaderMetadata metadata;

    public AbstractResourceLoader(ResourceLocation key, String path, String fileExtension) {
        this.key = key;
        this.path = path;
        this.fileExtension = fileExtension;
        this.keyFactory = (pathKey, element) -> getKeyFromPath(pathKey);
    }

    public abstract DataResult<T> parse(ResourceLocation path, Resource resource) throws Exception;

    @Contract("_ -> new")
    public final @NotNull CompletableFuture<Map<ResourceLocation, T>> parseAll(@NotNull ResourceManager manager) {
        return CompletableFuture.supplyAsync(() -> {
            metadata = new LoaderMetadata(-1, LoadStatus.RELOADING);

            LoadStatus[] status = { LoadStatus.OK };
            Map<ResourceLocation, T> parsed = new HashMap<>();

            long startTime = System.nanoTime();
            var resources = manager.listResources(path, loc
                    -> loc.getPath().endsWith('.' + fileExtension));
            resources.forEach((path, resource) -> {
                var result = tryParse(path, resource);
                if (result.isError()) {
                    status[0] = LoadStatus.FAIL;
                    LOGGER.warn("({}) Failed to load element '{}': {}", key, path, result.error().orElseThrow().message());
                    return;
                }
                var value = result.getOrThrow();
                parsed.put(keyFactory.getKeyOf(path, value), value);
            });

            metadata = new LoaderMetadata(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime), status[0]);

            return parsed;
        });
    }

    private DataResult<T> tryParse(ResourceLocation pathKey, Resource resource) {
        try {
            return parse(pathKey, resource);
        } catch (Exception e) {
            return DataResult.error(() -> "(%s): Failed to parse value '%s': %s: %s".formatted(key, pathKey, e.getClass().getName(), e.getMessage()));
        }
    }

    @Contract("_ -> new")
    private @NotNull ResourceLocation getKeyFromPath(@NotNull ResourceLocation pathKey) {
        return pathKey.withPath(pathKey.getPath().substring(path.length() + 1, pathKey.getPath().length() - fileExtension.length() - 1));
    }

    protected final void setKeyFactory(KeyFunction<T> keyFactory) {
        this.keyFactory = Objects.requireNonNull(keyFactory);
    }

    public final ResourceLocation key() {
        return key;
    }

    public final LoaderMetadata getMetadata() {
        return metadata;
    }

    public interface KeyFunction<T> {
        ResourceLocation getKeyOf(ResourceLocation path, T element);
    }
}
