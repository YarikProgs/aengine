package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record LoaderMetadata(ResourceLocation key, long lastLoadTimeMs, LoadStatus loadStatus) {
    public static final Codec<LoaderMetadata> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("key").forGetter(LoaderMetadata::key),
            Codec.LONG.fieldOf("lastLoadTimeMs").forGetter(LoaderMetadata::lastLoadTimeMs),
            LoadStatus.CODEC.fieldOf("loadStatus").forGetter(LoaderMetadata::loadStatus)
    ).apply(inst, LoaderMetadata::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LoaderMetadata> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, LoaderMetadata::key,
            ByteBufCodecs.VAR_LONG, LoaderMetadata::lastLoadTimeMs,
            LoadStatus.STREAM_CODEC, LoaderMetadata::loadStatus,
            LoaderMetadata::new
    ).cast();
}
