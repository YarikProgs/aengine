package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;

public class HorizontalSeparator extends UIElement {
    public HorizontalSeparator(IGuiTexture color) {
        getLayout().height(1).marginTop(1).marginBottom(1).marginLeft(2).marginRight(2);
        getStyle().backgroundTexture(color);
    }
}
