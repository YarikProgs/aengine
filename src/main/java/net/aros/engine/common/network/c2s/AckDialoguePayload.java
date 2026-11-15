package net.aros.engine.common.network.c2s;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.DialoguePhase;
import net.aros.engine.common.dialogue.playing.DialoguesTicker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Readiness for opening or closing one dialogue instance, never a phrase ack. */
public record AckDialoguePayload(UUID instanceId, DialoguePhase phase) implements CustomPacketPayload {
    public static final Type<AckDialoguePayload> TYPE = new Type<>(ArosEngine.id("ack_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AckDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AckDialoguePayload::instanceId,
            StreamCodec.of((RegistryFriendlyByteBuf buf, DialoguePhase phase) -> buf.writeEnum(phase),
                    buf -> buf.readEnum(DialoguePhase.class)), AckDialoguePayload::phase,
            AckDialoguePayload::new
    );

    public static void handle(AckDialoguePayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            DialoguesTicker.ackLifecycle(player, payload.instanceId(), payload.phase());
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
