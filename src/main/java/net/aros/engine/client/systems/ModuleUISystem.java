package net.aros.engine.client.systems;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.aros.engine.client.api.ui.ModularUIProvider;
import net.aros.engine.client.api.ui.ModuleUIProvider;
import net.aros.engine.client.api.ui.ScreenProvider;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface ModuleUISystem {
    void onUIRegister(Context ctx);

    class Context {
        private final Map<ResourceLocation, ModuleUIProvider> providers = new HashMap<>();

        public void registerScreen(ResourceLocation moduleKey, Function<Player, Screen> factory) {
            registerScreen(moduleKey, 0, factory);
        }

        public void registerScreen(ResourceLocation moduleKey, int requiredPermission, Function<Player, Screen> factory) {
            providers.put(moduleKey, new ScreenProvider(requiredPermission, factory));
        }

        public void registerModularUi(ResourceLocation moduleKey, Function<Player, ModularUI> factory) {
            registerModularUi(moduleKey, 0, factory);
        }

        public void registerModularUi(ResourceLocation moduleKey, int requiredPermission, Function<Player, ModularUI> factory) {
            providers.put(moduleKey, new ModularUIProvider(requiredPermission, factory));
        }

        public Map<ResourceLocation, ModuleUIProvider> getProviders() {
            return providers;
        }
    }
}
