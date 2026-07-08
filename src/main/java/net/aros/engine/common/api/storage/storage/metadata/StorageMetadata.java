package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record StorageMetadata(List<LoaderMetadata> loadersMetadata) {
    public static final Codec<StorageMetadata> CODEC = LoaderMetadata.CODEC.listOf().xmap(StorageMetadata::new, StorageMetadata::loadersMetadata);
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageMetadata> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, LoaderMetadata.STREAM_CODEC), StorageMetadata::loadersMetadata,
            StorageMetadata::new
    );

    public long lastLoadTimeMs() {
        long loadTimeMs = 0;
        for (LoaderMetadata loaderMetadata : loadersMetadata) {
            loadTimeMs += loaderMetadata.lastLoadTimeMs();
        }
        return loadTimeMs;
    }

    public LoadStatus loadStatus() {
        for (LoaderMetadata loaderMetadata : loadersMetadata) {
            if (loaderMetadata.loadStatus() != LoadStatus.OK)
                return loaderMetadata.loadStatus();
        }
        return LoadStatus.OK;
    }
}
