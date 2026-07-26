package net.aros.engine.common.api.value;

import com.mojang.serialization.Codec;
import net.aros.engine.common.init.AeRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ValueType<T>(Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    public static final Codec<ValueType<?>> CODEC = AeRegistries.VALUE_TYPE.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, ValueType<?>> STREAM_CODEC = ByteBufCodecs.registry(AeRegistries.VALUE_TYPE_KEY);

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Value<T> create(T value) {
        return new Value<>(this, value);
    }
}
