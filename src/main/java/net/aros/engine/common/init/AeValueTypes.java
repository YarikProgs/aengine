package net.aros.engine.common.init;

import com.mojang.serialization.Codec;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.value.ValueType;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

public class AeValueTypes {
    // Primitives
    public static final ValueType<Boolean> BOOL = registerFromCodec("bool", Codec.BOOL);
    public static final ValueType<Byte> BYTE = registerFromCodec("byte", Codec.BYTE);
    public static final ValueType<Short> SHORT = registerFromCodec("short", Codec.SHORT);
    public static final ValueType<Integer> INT = registerFromCodec("int", Codec.INT);
    public static final ValueType<Long> LONG = registerFromCodec("long", Codec.LONG);
    public static final ValueType<Float> FLOAT = registerFromCodec("float", Codec.FLOAT);
    public static final ValueType<Double> DOUBLE = registerFromCodec("double", Codec.DOUBLE);
    public static final ValueType<String> STRING = registerFromCodec("string", Codec.STRING);

    // Custom

    private static <T> @NotNull ValueType<T> registerFromCodec(String name, Codec<T> codec) {
        return Registry.register(AeRegistries.VALUE_TYPE, ArosEngine.id(name), new ValueType<>(codec));
    }

    public static void touch() {
    }
}
