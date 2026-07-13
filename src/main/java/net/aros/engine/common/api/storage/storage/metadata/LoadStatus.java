package net.aros.engine.common.api.storage.storage.metadata;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import static net.aros.engine.common.ArosEngine.translation;

public enum LoadStatus {
    OK,
    FAIL,
    RELOADING;

    public static final Codec<LoadStatus> CODEC = Codec.INT.xmap(i -> LoadStatus.values()[i], LoadStatus::ordinal);
    public static final StreamCodec<RegistryFriendlyByteBuf, LoadStatus> STREAM_CODEC = ByteBufCodecs.INT
            .map(i -> LoadStatus.values()[i], LoadStatus::ordinal).cast();

    public @NotNull Component getName() {
        return translation("load_status", name().toLowerCase());
    }
}
