package net.aros.engine.common.init;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.value.ValueType;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AeValueTypes {
    public static final ValueType<Boolean> BOOL = registerFromCodecs("bool", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final ValueType<Byte> BYTE = registerFromCodecs("byte", Codec.BYTE, ByteBufCodecs.BYTE);
    public static final ValueType<Short> SHORT = registerFromCodecs("short", Codec.SHORT, ByteBufCodecs.SHORT);
    public static final ValueType<Integer> INT = registerFromCodecs("int", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final ValueType<Long> LONG = registerFromCodecs("long", Codec.LONG, ByteBufCodecs.VAR_LONG);
    public static final ValueType<Float> FLOAT = registerFromCodecs("float", Codec.FLOAT, ByteBufCodecs.FLOAT);
    public static final ValueType<Double> DOUBLE = registerFromCodecs("double", Codec.DOUBLE, ByteBufCodecs.DOUBLE);
    public static final ValueType<String> STRING = registerFromCodecs("string", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final ValueType<ResourceLocation> RESOURCE_LOCATION = registerFromCodecs("resource_location", ResourceLocation.CODEC, ResourceLocation.STREAM_CODEC);
    public static final ValueType<CompoundTag> COMPOUND_TAG = registerFromCodecs("compound_tag", CompoundTag.CODEC, ByteBufCodecs.COMPOUND_TAG);

    private static <T> @NotNull ValueType<T> registerFromCodecs(String name, Codec<T> codec, StreamCodec<? super ByteBuf, T> streamCodec) {
        return Registry.register(AeRegistries.VALUE_TYPE, ArosEngine.id(name), new ValueType<>(codec, streamCodec.cast()));
    }

    public static void touch() {
    }
}
