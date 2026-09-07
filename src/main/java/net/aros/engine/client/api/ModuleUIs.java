package net.aros.engine.client.api;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.aros.engine.client.api.ui.ModularUIProvider;
import net.aros.engine.client.api.ui.ModuleUIProvider;
import net.aros.engine.client.api.ui.ScreenProvider;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModuleUIs {
    private static final Map<ResourceLocation, ModuleUIProvider> PROVIDERS = new HashMap<>();

    public static void registerScreen(ResourceLocation moduleKey, Function<Player, Screen> factory) {
        PROVIDERS.put(moduleKey, new ScreenProvider(factory));
    }

    public static void registerModularUi(ResourceLocation moduleKey, Function<Player, ModularUI> factory) {
        PROVIDERS.put(moduleKey, new ModularUIProvider(factory));
    }

    public static void register(ResourceLocation moduleKey, ModuleUIProvider provider) {
        PROVIDERS.put(moduleKey, provider);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<ResourceLocation, ModuleUIProvider> getProviders() {
        return Collections.unmodifiableMap(PROVIDERS);
    }
}
