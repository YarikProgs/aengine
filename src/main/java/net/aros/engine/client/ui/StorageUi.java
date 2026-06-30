package net.aros.engine.client.ui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import org.jetbrains.annotations.NotNull;

public class StorageUi {
    public static @NotNull ModularUI createModularUI() {
        UIElement root = new UIElement();

        root.addChildren(
                new Label().setText("Storages")
        );

        return ModularUI.of(UI.of(root));
    }
}
