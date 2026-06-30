package net.aros.engine.client.systems;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface UiSystem {
    void onUiRegister(Context ctx);

    class Context {
        private final Map<ResourceLocation, Supplier<Screen>> registeredUis = new HashMap<>();

        public void register(ResourceLocation key, Supplier<Screen> screenFactory) {
            registeredUis.put(key, Objects.requireNonNull(screenFactory));
        }

        public Map<ResourceLocation, Supplier<Screen>> getRegisteredUis() {
            return registeredUis;
        }
    }
}
