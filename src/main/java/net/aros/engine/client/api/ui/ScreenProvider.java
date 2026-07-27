package net.aros.engine.client.api.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class ScreenProvider implements ModuleUIProvider {
    private final Function<Player, Screen> factory;
    private final int requiredPermission;

    public ScreenProvider(int requiredPermission, Function<Player, Screen> factory) {
        this.requiredPermission = requiredPermission;
        this.factory = factory;
    }

    @Override
    public int requiredPermission() {
        return requiredPermission;
    }

    @Override
    public void openUI(Player player) {
        Screen screen = factory.apply(player);
        if (screen == null) return;
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(screen));
    }
}
