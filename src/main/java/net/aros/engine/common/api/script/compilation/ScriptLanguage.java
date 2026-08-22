package net.aros.engine.common.api.script.compilation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record ScriptLanguage(String id, String fileExtension) {
    public static final Codec<ScriptLanguage> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(ScriptLanguage::id),
            Codec.STRING.fieldOf("fileExtension").forGetter(ScriptLanguage::fileExtension)
    ).apply(inst, ScriptLanguage::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScriptLanguage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ScriptLanguage::id,
            ByteBufCodecs.STRING_UTF8, ScriptLanguage::fileExtension,
            ScriptLanguage::new
    );

    public ScriptLanguage {
        id = requireToken(id, "id");
        fileExtension = normalizeExtension(fileExtension);
    }

    private static String requireToken(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeExtension(String extension) {
        Objects.requireNonNull(extension, "fileExtension");
        String normalized = extension.trim();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
