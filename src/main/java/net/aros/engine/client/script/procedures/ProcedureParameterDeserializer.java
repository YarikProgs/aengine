package net.aros.engine.client.script.procedures;

import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ProcedureParameterDeserializer<T> {
    DataResult<T> deserialize(RegistryFriendlyByteBuf buffer);

    @Contract(pure = true)
    static <T> @NotNull ProcedureParameterDeserializer<T> streamCodec(@NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return buffer -> DataResult.success(streamCodec.decode(buffer));
    }
}
