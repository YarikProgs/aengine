package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record StorageMetadata(Map<ResourceLocation, LoaderMetadata> loadersMetadata) {
    public static final Codec<StorageMetadata> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, LoaderMetadata.CODEC)
            .xmap(StorageMetadata::new, StorageMetadata::loadersMetadata);

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageMetadata> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, LoaderMetadata.STREAM_CODEC), StorageMetadata::loadersMetadata,
            StorageMetadata::new
    );

    public long lastLoadTimeMs() {
        boolean anyLoaded = false;
        long loadTimeMs = 0;
        for (LoaderMetadata loaderMetadata : loadersMetadata.values()) {
            if (loaderMetadata.lastLoadTimeMs() == -1) continue;

            loadTimeMs += loaderMetadata.lastLoadTimeMs();
            anyLoaded = true;
        }
        return anyLoaded ? loadTimeMs : -1;
    }

    public LoadStatus loadStatus() {
        for (LoaderMetadata loaderMetadata : loadersMetadata.values()) {
            if (loaderMetadata.loadStatus() != LoadStatus.OK)
                return loaderMetadata.loadStatus();
        }
        return LoadStatus.OK;
    }
}
