package net.aros.engine.client.init;

import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.client.gui.ui.ValuesUI;
import net.aros.engine.client.ui.ModuleUIs;
import net.aros.engine.common.ArosEngine;

public class AeUIs {
    public static void init() {
        ModuleUIs.registerModularUi(ArosEngine.id("storage"), player -> StorageUI.createModularUI());
        ModuleUIs.registerModularUi(ArosEngine.id("values"), player -> player.hasPermissions(2) ? ValuesUI.createModularUI() : null);
    }
}
