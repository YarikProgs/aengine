package net.aros.engine.client.api.ui;

import net.minecraft.world.entity.player.Player;

public interface ModuleUIProvider {
    void openUI(Player player);

   int requiredPermission();
}
