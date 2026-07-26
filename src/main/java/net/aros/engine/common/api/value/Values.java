package net.aros.engine.common.api.value;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

// Wrapper only for RPCs. May be removed later
public record Values(Map<String, Value<?>> map) {
    public static final Codec<Values> CODEC = Codec.unboundedMap(Codec.STRING, Value.CODEC).xmap(Values::new, Values::map);
    public static final StreamCodec<RegistryFriendlyByteBuf, Values> STREAM_CODEC = ByteBufCodecs
            .map((IntFunction<Map<String, Value<?>>>) HashMap::new, ByteBufCodecs.STRING_UTF8, Value.STREAM_CODEC)
            .map(Values::new, Values::map);
}
