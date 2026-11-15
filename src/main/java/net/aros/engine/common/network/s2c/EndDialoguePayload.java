package net.aros.engine.common.network.s2c;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.DialogueInfo;
import net.aros.engine.common.dialogue.playing.EndReason;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record EndDialoguePayload(DialogueInfo dialogue, EndReason reason) implements CustomPacketPayload {
    public static final Type<EndDialoguePayload> TYPE = new Type<>(ArosEngine.id("end_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EndDialoguePayload> STREAM_CODEC = StreamCodec.composite(
            DialogueInfo.STREAM_CODEC, EndDialoguePayload::dialogue,
            StreamCodec.of(FriendlyByteBuf::writeEnum,
                    buf -> buf.readEnum(EndReason.class)), EndDialoguePayload::reason,
            EndDialoguePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
