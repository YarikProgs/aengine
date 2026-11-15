package net.aros.engine.common.dialogue;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Client-visible identity of one execution, including restarts of the same script. */
public record DialogueInfo(ResourceLocation dialogueId, String dialect, UUID instanceId) {
    public static final StreamCodec<RegistryFriendlyByteBuf, DialogueInfo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, DialogueInfo::dialogueId,
            ByteBufCodecs.STRING_UTF8, DialogueInfo::dialect,
            UUIDUtil.STREAM_CODEC, DialogueInfo::instanceId,
            DialogueInfo::new
    );
}
