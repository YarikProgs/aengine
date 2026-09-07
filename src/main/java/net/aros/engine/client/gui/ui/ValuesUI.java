package net.aros.engine.client.gui.ui;

import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.OreSprites;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.ui.elements.ValueTreeList;
import net.aros.engine.client.gui.ui.elements.ValueViewer;
import net.aros.engine.common.value.Values;
import net.minecraft.client.Minecraft;
import org.appliedenergistics.yoga.YogaEdge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.aros.engine.common.ArosEngine.translation;

public class ValuesUI extends UIElement {
    public final SplittableWindow rootWindow;
    public SplittableWindow leftWindow, centerWindow;

    public final UIElement top = new UIElement();
    public final UIElement icon = new UIElement();
    public final UIElement menuContainer = new UIElement();
    public final UIElement topPlaceholder = new UIElement();
    public final UIElement buttonContainer = new UIElement();
    public final Button closeButton = new Button();

    public final ValueTreeList valueTreeList = new ValueTreeList();
    public final ValueViewer valueViewer = new ValueViewer();

    public ValuesUI() {
        getLayout().widthPercent(100).heightPercent(100);

        rootWindow = new SplittableWindow().setImmortal(true).setAnchorId("root");
        var split = rootWindow
                .splitStyle(style -> style.percentage(25).minPercentage(5).maxPercentage(95))
                .splitNew(YogaEdge.LEFT);
        leftWindow = split.getFirst().setImmortal(true).setAnchorId("left");
        centerWindow = split.getSecond().setImmortal(true).setAnchorId("center");

        leftWindow.getLeftTop().addView(createValueExplorerView());
        centerWindow.getLeftTop().addView(createValueViewerView());

        addEventListener(UIEvents.MUI_CHANGED, e -> fetchAllValues());

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

    private @NotNull View createValueViewerView() {
        var view = new View();
        view.setDynamicName(() -> translation("view", "value"));
        view.addChild(valueViewer);
        return view;
    }

    private @NotNull View createValueExplorerView() {
        var view = new View();
        view.setDynamicName(() -> translation("view", "value_explorer"));

        view.addChild(
                new ScrollerView()
                        .addScrollViewChild(
                                valueTreeList
                                        .setOnSelectedChanged(nodes -> {
                                            if (nodes.size() != 1) return;
                                            var node = nodes.iterator().next();
                                            if (node.getContent() == null) return;
                                            valueViewer.setSelectedValue(node.getContent());
                                        })
                        )
                        .layout(layout -> {
                            layout.widthPercent(100);
                            layout.heightPercent(100);
                        })
        );

        return view;
    }

    private static void fetchAllValues() {
        RPCPacketDistributor.rpcToServer("get_all_values", "receive_all_values");
    }

    @RPCPacket("receive_all_values")
    public static void receiveAllValues(Values values) {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen) ||
                !(modularUIScreen.modularUI.ui.rootElement instanceof ValuesUI ui)) return;
        ui.valueTreeList.setValues(values.map());
    }

    @Contract(" -> new")
    public static @NotNull ModularUI createModularUI() {
        return ModularUI.of(UI.of(new ValuesUI(), StylesheetManager.ORE_MERGED));
    }
}
