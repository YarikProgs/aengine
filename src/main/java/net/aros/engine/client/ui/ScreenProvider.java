package net.aros.engine.client.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class ScreenProvider implements ModuleUIProvider {
    private final Function<Player, Screen> factory;

    public ScreenProvider(Function<Player, Screen> factory) {
        this.factory = factory;
    }

    @Override
    public Screen createUI(Player player) {
        return factory.apply(player);
    }
}
