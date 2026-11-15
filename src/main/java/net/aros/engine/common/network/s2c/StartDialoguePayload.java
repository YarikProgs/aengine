package net.aros.engine.common.network.s2c;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.DialogueInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record StartDialoguePayload(DialogueInfo dialogue) implements CustomPacketPayload {
    public static final Type<StartDialoguePayload> TYPE = new Type<>(ArosEngine.id("start_dialogue"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartDialoguePayload> STREAM_CODEC =
            DialogueInfo.STREAM_CODEC.map(StartDialoguePayload::new, StartDialoguePayload::dialogue);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
