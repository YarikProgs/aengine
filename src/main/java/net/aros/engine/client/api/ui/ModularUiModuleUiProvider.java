package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class ModularUiModuleUiProvider implements ModuleUiProvider {
    private final Function<Player, ModularUI> factory;

    public ModularUiModuleUiProvider(Function<Player, ModularUI> factory) {
        this.factory = factory;
    }

    @Override
    public void openUi(Player player) {
        Minecraft.getInstance().setScreen(new ModularUIScreen(factory.apply(player), Component.empty()));
    }
}
