package net.aros.engine.common.api.script.compilation;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record AeScript(ScriptLanguage language, Runnable runnable) implements Runnable {
    public static final Codec<AeScript> CODEC = ScriptLanguage.CODEC.xmap(AeScript::dummy, AeScript::language);
    public static final StreamCodec<RegistryFriendlyByteBuf, AeScript> STREAM_CODEC = ScriptLanguage.STREAM_CODEC.map(AeScript::dummy, AeScript::language);

    public static AeScript dummy(ScriptLanguage language) {
        return new AeScript(language, () -> {
            throw new RuntimeException("Dummy script call. Either someone called script from client side, or tried to persist it");
        });
    }

    public AeScript {
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(runnable, "runnable");
    }

    @Override
    public void run() {
        runnable.run();
    }
}
