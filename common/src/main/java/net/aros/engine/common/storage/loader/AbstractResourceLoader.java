package net.aros.engine.common.storage.loader;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
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

public abstract class AbstractResourceLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String path;
    private final String fileExtension;
    private KeyFunction<T> keyFactory;

    public AbstractResourceLoader(String path, String fileExtension) {
        this.path = path;
        this.fileExtension = fileExtension;
        this.keyFactory = (pathKey, element) -> getKeyFromPath(pathKey);
    }

    public abstract DataResult<T> parse(ResourceLocation path, Resource resource) throws Exception;

    @Contract("_ -> new")
    public final @NotNull CompletableFuture<Map<ResourceLocation, T>> parseAll(@NotNull ResourceManager manager) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, T> parsed = new HashMap<>();

            var resources = manager.listResources(path, loc
                    -> loc.getPath().endsWith('.' + fileExtension));
            resources.forEach((path, resource) -> {
                var result = tryParse(path, resource);
                if (result.isError()) {
                    LOGGER.warn("({}) Failed to load element '{}': {}", getClass(), path, result.error().orElseThrow().message());
                    return;
                }
                var value = result.getOrThrow();
                parsed.put(keyFactory.getKeyOf(path, value), value);
            });

            return parsed;
        });
    }

    private DataResult<T> tryParse(ResourceLocation pathKey, Resource resource) {
        try {
            return parse(pathKey, resource);
        } catch (Exception e) {
            return DataResult.error(() -> "(%s): Failed to parse value '%s': %s: %s".formatted(getClass().getName(), pathKey, e.getClass().getName(), e.getMessage()));
        }
    }

    @Contract("_ -> new")
    private @NotNull ResourceLocation getKeyFromPath(@NotNull ResourceLocation pathKey) {
        return pathKey.withPath(pathKey.getPath().substring(path.length() + 1, pathKey.getPath().length() - fileExtension.length() - 1));
    }

    protected final void setKeyFactory(KeyFunction<T> keyFactory) {
        this.keyFactory = Objects.requireNonNull(keyFactory);
    }

    public interface KeyFunction<T> {
        ResourceLocation getKeyOf(ResourceLocation path, T element);
    }
}
