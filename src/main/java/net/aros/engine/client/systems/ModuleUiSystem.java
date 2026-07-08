package net.aros.engine.client.systems;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.aros.engine.client.api.ui.ModularUiModuleUiProvider;
import net.aros.engine.client.api.ui.ModuleUiProvider;
import net.aros.engine.client.api.ui.ScreenModuleUiProvider;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ModuleUiSystem {
    void onModuleUiRegister(Context ctx);

    class Context {
        private final Map<ResourceLocation, ModuleUiProvider> providers = new HashMap<>();

        public void registerScreen(ResourceLocation moduleKey, Function<Player, Screen> factory) {
            providers.put(moduleKey, new ScreenModuleUiProvider(factory));
        }

        public void registerModularUi(ResourceLocation moduleKey, Function<Player, ModularUI> factory) {
            providers.put(moduleKey, new ModularUiModuleUiProvider(factory));
        }

        public Map<ResourceLocation, ModuleUiProvider> getProviders() {
            return providers;
        }
    }
}
