package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class ModularUIProvider implements ModuleUIProvider {
    private final Function<Player, ModularUI> factory;
    private final int requiredPermission;

    public ModularUIProvider(int requiredPermission, Function<Player, ModularUI> factory) {
        this.requiredPermission = requiredPermission;
        this.factory = factory;
    }

    public ModularUIProvider(Function<Player, ModularUI> factory) {
        this(0, factory);
    }

    @Override
    public void openUI(Player player) {
        Minecraft.getInstance().setScreen(new ModularUIScreen(factory.apply(player), Component.empty()));
    }

    @Override
    public int requiredPermission() {
        return requiredPermission;
    }
}
