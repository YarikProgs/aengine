package net.aros.engine.client.modules;

import net.aros.engine.client.gui.ui.StorageEditor;
import net.aros.engine.client.systems.ModuleUISystem;
import net.aros.engine.common.ArosEngine;
import org.jetbrains.annotations.NotNull;

public class StorageClientModule implements ModuleUISystem {
    @Override
    public void onUIRegister(@NotNull ModuleUISystem.Context ctx) {
        ctx.registerModularUi(ArosEngine.id("storage"), player -> StorageEditor.createModularUI());
    }
}
