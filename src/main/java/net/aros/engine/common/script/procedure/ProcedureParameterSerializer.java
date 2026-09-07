package net.aros.engine.common.script.procedure;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ProcedureParameterSerializer<T> {
    void serialize(RegistryFriendlyByteBuf buffer, T parameter);

    @Contract(pure = true)
    static <T> @NotNull ProcedureParameterSerializer<T> streamCodec(@NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return streamCodec::encode;
    }
}
