package net.aros.engine.client.gui.ui;

import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.OreSprites;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import org.appliedenergistics.yoga.YogaEdge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ValuesUI extends UIElement {
    public final SplittableWindow rootWindow;
    public SplittableWindow leftWindow, centerWindow;

    public final UIElement top = new UIElement();
    public final UIElement icon = new UIElement();
    public final UIElement menuContainer = new UIElement();
    public final UIElement topPlaceholder = new UIElement();
    public final UIElement buttonContainer = new UIElement();
    public final Button closeButton = new Button();

    public ValuesUI() {
        getLayout().widthPercent(100).heightPercent(100);

        rootWindow = new SplittableWindow().setImmortal(true).setAnchorId("root");
        var split = rootWindow
                .splitStyle(style -> style.percentage(25).minPercentage(5).maxPercentage(95))
                .splitNew(YogaEdge.LEFT);
        leftWindow = split.getFirst().setImmortal(true).setAnchorId("left");
        centerWindow = split.getSecond().setImmortal(true).setAnchorId("center");

        addChildren(
                top.layout(layout -> {
                            layout.paddingAll(1);
                            layout.widthPercent(100);
                            layout.height(15);
                            layout.flexDirection(FlexDirection.ROW);
                            layout.gapAll(2);
                        }).style(style -> style.backgroundTexture(OreSprites.RECT))
                        .addChildren(
                                icon.layout(layout -> {
                                    layout.width(11);
                                    layout.height(11);
                                    layout.marginAll(1);
                                    layout.marginHorizontal(5);
                                }).style(style -> style.backgroundTexture(new SpriteTexture())),
                                menuContainer.layout(layout -> {
                                    layout.heightPercent(100);
                                    layout.flexDirection(FlexDirection.ROW);
                                    layout.gapAll(2);
                                }),
                                topPlaceholder.layout(layout -> layout.flex(1)), // placeholder
                                buttonContainer.layout(layout -> {
                                    layout.flexDirection(FlexDirection.ROW);
                                    layout.alignItems(AlignItems.CENTER);
                                    layout.gapAll(2);
                                    layout.marginRight(1);
                                }).addChildren(
                                        closeButton.noText().addPreIcon(Icons.WINDOW_CLOSE)
                                                .layout(layout -> layout.height(12))
                                                .addClass("__white_icon__")
                                )
                        ),

                new UIElement().layout(layout -> {
                    layout.widthPercent(100);
                    layout.flex(1);
                }).addChild(rootWindow)
        );
        closeButton.setOnClick(e -> {
            if (getModularUI() != null && getModularUI().getScreen() != null) {
                getModularUI().getScreen().onClose();
            }
        });
    }

    @Contract(" -> new")
    public static @NotNull ModularUI createModularUI() {
        return ModularUI.of(UI.of(new ValuesUI(), StylesheetManager.ORE_MERGED));
    }
}
