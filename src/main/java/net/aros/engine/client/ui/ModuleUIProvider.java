package net.aros.engine.client.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface ModuleUIProvider {
    @Nullable
    Screen createUI(Player player);
}
