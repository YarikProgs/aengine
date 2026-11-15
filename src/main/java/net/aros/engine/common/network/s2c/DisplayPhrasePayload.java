package net.aros.engine.common.network.s2c;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.dialogue.DialogueDialects;
import net.aros.engine.common.dialogue.DialogueLine;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** One per-viewer phrase. P and L match the dialect's codecs; Unit when unused. */
public record DisplayPhrasePayload<P, L>(
        ResourceLocation dialogueId,
        String dialect,
        UUID instanceId,
        int phraseIndex,
        List<DialogueLine<L>> lines,
        P extraInfo
) implements CustomPacketPayload {
    public static final Type<DisplayPhrasePayload<?, ?>> TYPE = new Type<>(ArosEngine.id("display_phrase"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayPhrasePayload<?, ?>> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.<RegistryFriendlyByteBuf>cast().dispatch(
                    DisplayPhrasePayload::dialect, DisplayPhrasePayload::codecForDialect);

    public DisplayPhrasePayload {
        lines = List.copyOf(lines);
        Objects.requireNonNull(extraInfo, "Phrase extra info");
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // The dialect name selects both extra info types.
    private static @NotNull StreamCodec<RegistryFriendlyByteBuf, DisplayPhrasePayload<?, ?>> codecForDialect(String name) {
        var dialect = DialogueDialects.get(name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dialogue dialect: " + name));
        return (StreamCodec) codec(name, dialect.phrasewiseExtraInfoCodec(), dialect.linewiseExtraInfoCodec());
    }

    private static <P, L> @NotNull StreamCodec<RegistryFriendlyByteBuf, DisplayPhrasePayload<P, L>> codec(
            String dialect, StreamCodec<? super RegistryFriendlyByteBuf, P> phraseCodec,
            StreamCodec<? super RegistryFriendlyByteBuf, L> lineCodec) {
        StreamCodec<RegistryFriendlyByteBuf, DialogueLine<L>> line = StreamCodec.composite(
                ComponentSerialization.TRUSTED_STREAM_CODEC, DialogueLine::text,
                lineCodec, DialogueLine::extraInfo, DialogueLine::new);
        return StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, DisplayPhrasePayload::dialogueId,
                UUIDUtil.STREAM_CODEC, DisplayPhrasePayload::instanceId,
                ByteBufCodecs.VAR_INT, DisplayPhrasePayload::phraseIndex,
                ByteBufCodecs.collection(ArrayList::new, line), DisplayPhrasePayload::lines,
                phraseCodec, DisplayPhrasePayload::extraInfo,
                (id, instance, index, lines, extra) -> new DisplayPhrasePayload<>(id, dialect, instance, index, lines, extra));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
