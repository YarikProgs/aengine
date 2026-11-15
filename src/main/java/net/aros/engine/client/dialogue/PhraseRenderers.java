package net.aros.engine.client.dialogue;

import net.aros.engine.client.events.RegisterPhraseRendererEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PhraseRenderers {
    private static final Map<String, PhraseRenderer<?, ?>> RENDERERS = new ConcurrentHashMap<>();

    public static void reload() {
        RENDERERS.clear();
        RENDERERS.putAll(ModLoader.postEventWithReturn(new RegisterPhraseRendererEvent()).renderers());
    }

    public static @NotNull Optional<PhraseRenderer<?, ?>> get(String name) {
        return Optional.ofNullable(RENDERERS.get(name));
    }
}
