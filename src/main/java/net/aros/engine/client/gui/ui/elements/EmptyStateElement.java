package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public class EmptyStateElement extends UIElement {
    public final Label label = new Label();
    public final UIElement image = new UIElement();

    public EmptyStateElement() {
        getLayout().widthPercent(100).heightPercent(100);
        addChildren(
                image
                        .layout(layout -> {
                            layout.alignSelf(AlignItems.CENTER);
                            layout.flex(0.5f);
                            layout.maxWidthPercent(100);
                            layout.setAspectRatio(1);
                        }),
                label
                        .textStyle(textStyle -> {
                            textStyle.textAlignHorizontal(Horizontal.CENTER);
                            textStyle.textShadow(false);
                            textStyle.textWrap(TextWrap.WRAP);
                        })
                        .layout(layout -> {
                            layout.maxWidthPercent(100);
                            layout.flex(0.5f);
                            layout.marginTop(10);
                            layout.widthPercent(100);
                            layout.heightPercent(50);
                        })
                        .style(style -> style.color(0xff9b9b9b))
        );
    }

    public EmptyStateElement setText(Component text) {
        label.setText(text);
        return this;
    }

    public EmptyStateElement setImage(IGuiTexture texture) {
        image.getStyle().backgroundTexture(texture);
        return this;
    }
}
