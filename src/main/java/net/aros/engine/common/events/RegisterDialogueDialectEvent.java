package net.aros.engine.common.events;

import net.aros.engine.common.dialogue.DialogueDialect;
import net.aros.engine.common.dialogue.LineInterpreter;
import net.aros.haru.runtime.AbstractTextRenderer;
import net.aros.haru.runtime.FuzzyPolicy;
import net.aros.haru.value.HaruFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class RegisterDialogueDialectEvent extends Event implements IModBusEvent {
    private final Map<String, DialogueDialect> dialects = new HashMap<>();

    public void create(String dialect, @NotNull Consumer<Builder> consumer) {
        var builder = new Builder(dialect);
        consumer.accept(builder);
        register(builder);
    }

    private void register(@NotNull Builder builder) {
        dialects.put(builder.name, builder.build());
    }

    public Map<String, DialogueDialect> dialects() {
        return dialects;
    }

    public static class Builder {
        private final String name;
        private final Map<String, HaruFunction> functions = new HashMap<>();
        private AbstractTextRenderer.Factory textRendererFactory;
        private LineInterpreter lineInterpreter;
        private FuzzyPolicy fuzzyPolicy = FuzzyPolicy.USE_ORIGINAL;
        private StreamCodec<? super RegistryFriendlyByteBuf, ?> phrasewiseExtraInfoCodec = DialogueDialect.UNIT_CODEC;
        private StreamCodec<? super RegistryFriendlyByteBuf, ?> linewiseExtraInfoCodec = DialogueDialect.UNIT_CODEC;

        public Builder(String name) {
            this.name = name;
        }

        public void define(String name, HaruFunction function) {
            if (functions.containsKey(name)) {
                throw new IllegalArgumentException("Function '" + name + "' already defined in " + name);
            }
            Objects.requireNonNull(name, "Function name");
            Objects.requireNonNull(function, "Function");
            functions.put(name, function);
        }

        public void setLineInterpreter(LineInterpreter lineInterpreter) {
            if (this.lineInterpreter != null) {
                throw new IllegalArgumentException("Line Dispatcher already registered in " + name);
            }
            this.lineInterpreter = lineInterpreter;
        }

        public void setTextRenderer(AbstractTextRenderer.Factory textRendererFactory) {
            if (this.textRendererFactory != null) {
                throw new IllegalArgumentException("TextRenderer already registered in " + name);
            }
            this.textRendererFactory = Objects.requireNonNull(textRendererFactory, "TextRenderer");
        }

        public void fuzzyPolicy(FuzzyPolicy fuzzyPolicy) {
            this.fuzzyPolicy = Objects.requireNonNull(fuzzyPolicy, "FuzzyPolicy");
        }

        /** Register identically on both sides. Defaults to Unit when unused. */
        public <P> void setPhrasewiseExtraInfoCodec(StreamCodec<? super RegistryFriendlyByteBuf, P> codec) {
            phrasewiseExtraInfoCodec = Objects.requireNonNull(codec, "Phrasewise extra info codec");
        }

        /** Register on both sides, independently of the phrasewise codec. */
        public <L> void setLinewiseExtraInfoCodec(StreamCodec<? super RegistryFriendlyByteBuf, L> codec) {
            linewiseExtraInfoCodec = Objects.requireNonNull(codec, "Linewise extra info codec");
        }

        public DialogueDialect build() {
            return new DialogueDialect(name, functions, textRendererFactory, lineInterpreter, fuzzyPolicy,
                    phrasewiseExtraInfoCodec, linewiseExtraInfoCodec);
        }
    }
}
