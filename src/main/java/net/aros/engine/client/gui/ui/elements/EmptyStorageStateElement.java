package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.AlignItems;

import static net.aros.engine.common.ArosEngine.translation;

public class EmptyStorageStateElement extends UIElement {
    public EmptyStorageStateElement() {
        getLayout().widthPercent(100).heightPercent(100);
        addChildren(
                new UIElement()
                        .layout(layout -> {
                            layout.alignSelf(AlignItems.CENTER);
                            layout.flex(0.5f);
                            layout.maxWidthPercent(100);
                            layout.setAspectRatio(1);
                        })
                        .style(style -> {
                            style.backgroundTexture(StorageViewer.CHEST);
                            style.opacity(1f);
                        }),
                new Label()
                        .setText(translation("text", "no_storage"))
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
                        .style(style -> {
                            style.color(0xff9b9b9b);
                        })
        );
    }
}
