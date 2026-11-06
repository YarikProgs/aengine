package net.aros.engine.common.network.s2c;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.script.procedure.ProcedureEncoding;
import net.aros.engine.common.script.procedure.ProcedureSerializationException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ProcedureCallPayload(ResourceLocation id, byte[] args) implements CustomPacketPayload {
    public static final Type<ProcedureCallPayload> TYPE = new Type<>(ArosEngine.id("client_call"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, ProcedureCallPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ProcedureCallPayload::id,
            ByteBufCodecs.BYTE_ARRAY, ProcedureCallPayload::args,
            ProcedureCallPayload::new
    );

    public static Optional<ProcedureCallPayload> create(RegistryAccess access, ResourceLocation id, Object[] args) {
        try {
            var bytes = ProcedureEncoding.encodeProcedureArgs(id, access, args);
            return Optional.of(new ProcedureCallPayload(id, bytes));
        } catch (ProcedureSerializationException ignored) {
        }
        return Optional.empty();
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
