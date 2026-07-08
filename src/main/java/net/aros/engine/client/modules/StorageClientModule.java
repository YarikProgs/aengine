package net.aros.engine.client.modules;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import net.aros.engine.client.gui.ui.StorageEditor;
import net.aros.engine.client.systems.ModuleUiSystem;
import net.aros.engine.common.ArosEngine;
import org.jetbrains.annotations.NotNull;

public class StorageClientModule implements ModuleUiSystem {
    @Override
    public void onModuleUiRegister(@NotNull ModuleUiSystem.Context ctx) {
        ctx.registerModularUi(ArosEngine.id("storage"), player -> ModularUI.of(UI.of(new StorageEditor())));
    }
}
