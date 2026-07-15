package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public record StorageMetadataMap(Map<StorageKey<?>, StorageMetadata> value) {
    public static final Codec<StorageMetadataMap> CODEC = Codec.unboundedMap(StorageKey.CODEC, StorageMetadata.CODEC)
            .xmap(StorageMetadataMap::new, StorageMetadataMap::value);
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageMetadataMap> STREAM_CODEC = ByteBufCodecs
            .map((IntFunction<Map<StorageKey<?>, StorageMetadata>>) HashMap::new, StorageKey.STREAM_CODEC, StorageMetadata.STREAM_CODEC)
            .map(StorageMetadataMap::new, StorageMetadataMap::value);
}
