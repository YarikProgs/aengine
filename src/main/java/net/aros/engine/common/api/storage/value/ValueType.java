package net.aros.engine.common.api.storage.value;

import com.mojang.serialization.Codec;
import net.aros.engine.common.init.AEngineRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ValueType<T>(Codec<T> valueCodec) {
    public static final Codec<ValueType<?>> CODEC = AEngineRegistries.VALUE_TYPE.byNameCodec();

    @Contract(value = "_ -> new", pure = true)
    public @NotNull Value<T> create(T value) {
        return new Value<>(this, value);
    }
}
