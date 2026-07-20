package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LoaderMetadata(long lastLoadTimeMs, LoadStatus loadStatus) {
    public static final Codec<LoaderMetadata> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.fieldOf("lastLoadTimeMs").forGetter(LoaderMetadata::lastLoadTimeMs),
            LoadStatus.CODEC.fieldOf("loadStatus").forGetter(LoaderMetadata::loadStatus)
    ).apply(inst, LoaderMetadata::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LoaderMetadata> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, LoaderMetadata::lastLoadTimeMs,
            LoadStatus.STREAM_CODEC, LoaderMetadata::loadStatus,
            LoaderMetadata::new
    ).cast();
}
