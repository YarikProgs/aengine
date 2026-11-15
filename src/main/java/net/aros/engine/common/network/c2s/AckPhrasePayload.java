package net.aros.engine.common.network.c2s;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.playing.DialoguesTicker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Client acknowledges that the player has read a phrase. */
public record AckPhrasePayload(UUID instanceId, int phraseIndex) implements CustomPacketPayload {
    public static final Type<AckPhrasePayload> TYPE = new Type<>(ArosEngine.id("ack_phrase"));

    public static final StreamCodec<? super RegistryFriendlyByteBuf, AckPhrasePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AckPhrasePayload::instanceId,
            ByteBufCodecs.VAR_INT, AckPhrasePayload::phraseIndex,
            AckPhrasePayload::new
    );

    public static void handle(AckPhrasePayload payload, @NotNull IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        DialoguesTicker.ack(player, payload.instanceId(), payload.phraseIndex());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
