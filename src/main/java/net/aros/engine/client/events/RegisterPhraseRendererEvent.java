package net.aros.engine.client.events;

import net.aros.engine.client.dialogue.PhraseRenderer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterPhraseRendererEvent extends Event implements IModBusEvent {
    private final Map<String, PhraseRenderer<?, ?>> renderers = new HashMap<>();

    public void register(String name, PhraseRenderer<?, ?> renderer) {
        if (this.renderers.containsKey(name)) {
            throw new IllegalArgumentException("PhraseRenderer already registered in " + name);
        }
        renderers.put(name, Objects.requireNonNull(renderer));
    }

    public Map<String, PhraseRenderer<?, ?>> renderers() {
        return renderers;
    }
}
