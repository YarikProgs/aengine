package net.aros.engine.common.api.storage.storage.type;

import com.mojang.serialization.Codec;
import net.aros.engine.common.init.AeRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StorageType<T>(Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, Class<T> type) {
    public static final Codec<StorageType<?>> CODEC = AeRegistries.STORAGE_TYPE.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageType<?>> STREAM_CODEC = ByteBufCodecs.registry(AeRegistries.STORAGE_TYPE_KEY);
}
