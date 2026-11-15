package net.aros.engine.common.dialogue;

import com.mojang.datafixers.util.Unit;
import net.aros.haru.runtime.AbstractTextRenderer;
import net.aros.haru.runtime.FuzzyPolicy;
import net.aros.haru.value.HaruFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record DialogueDialect(String name, Map<String, HaruFunction> functions,
                              AbstractTextRenderer.Factory textRendererFactory, LineInterpreter lineInterpreter,
                              FuzzyPolicy fuzzyPolicy,
                              StreamCodec<? super RegistryFriendlyByteBuf, ?> phrasewiseExtraInfoCodec,
                              StreamCodec<? super RegistryFriendlyByteBuf, ?> linewiseExtraInfoCodec) {
    public static final StreamCodec<RegistryFriendlyByteBuf, Unit> UNIT_CODEC = StreamCodec.unit(Unit.INSTANCE);

    public DialogueDialect {
        functions = Collections.unmodifiableMap(functions);
        Objects.requireNonNull(phrasewiseExtraInfoCodec, "Phrasewise extra info codec");
        Objects.requireNonNull(linewiseExtraInfoCodec, "Linewise extra info codec");
    }

    public DialogueDialect(String name, Map<String, HaruFunction> functions,
                           AbstractTextRenderer.Factory textRendererFactory, LineInterpreter lineInterpreter,
                           FuzzyPolicy fuzzyPolicy) {
        this(name, functions, textRendererFactory, lineInterpreter, fuzzyPolicy, UNIT_CODEC, UNIT_CODEC);
    }
}
