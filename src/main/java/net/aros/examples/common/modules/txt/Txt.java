package net.aros.examples.common.modules.txt;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Txt(String content) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Txt> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Txt::new, Txt::content).cast();
    public static final Codec<Txt> CODEC = Codec.STRING.xmap(Txt::new, Txt::content);
}
