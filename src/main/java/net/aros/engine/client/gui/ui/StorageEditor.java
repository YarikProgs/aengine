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
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.OreSprites;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.mojang.logging.LogUtils;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.ui.elements.LoadersViewer;
import net.aros.engine.client.gui.ui.elements.StorageTreeList;
import net.aros.engine.client.gui.ui.elements.StorageViewer;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.minecraft.client.Minecraft;
import org.appliedenergistics.yoga.YogaEdge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageEditor extends UIElement {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final SplittableWindow rootWindow;
    public SplittableWindow leftWindow, centerWindow;

    public final StorageTreeList storageTreeList = new StorageTreeList();
    public final StorageViewer storageViewer = new StorageViewer();
    public final LoadersViewer loadersViewer = new LoadersViewer();

    public final UIElement top = new UIElement();
    public final UIElement icon = new UIElement();
    public final UIElement menuContainer = new UIElement();
    public final UIElement topPlaceholder = new UIElement();
    public final UIElement buttonContainer = new UIElement();
    public final Button closeButton = new Button();

    private final Map<StorageKey<?>, StorageMetadata> serverStoragesMetadata = new HashMap<>();
    private final Map<StorageKey<?>, StorageMetadata> clientStoragesMetadata = new HashMap<>();

    public StorageEditor() {
        RPCPacketDistributor.rpcToServer("storageEditorSession$create");
        getLayout().widthPercent(100).heightPercent(100);

        rootWindow = new SplittableWindow().setImmortal(true).setAnchorId("root");
        var split = rootWindow
                .splitStyle(style -> style.percentage(25).minPercentage(5).maxPercentage(95))
                .splitNew(YogaEdge.LEFT);
        leftWindow = split.getFirst().setImmortal(true).setAnchorId("left");
        centerWindow = split.getSecond().setImmortal(true).setAnchorId("center");

        leftWindow.getLeftTop().addView(createStorageListView());
        centerWindow.getLeftTop().addView(createCurrentStorageView());
        centerWindow.getLeftTop().addView(createLoadersView());

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
                RPCPacketDistributor.rpcToServer("storageEditorSession$remove");
                getModularUI().getScreen().onClose();
            }
        });
    }

    private @NotNull View createStorageListView() {
        var storagesView = new View();
        storagesView.setDynamicName(() -> translation("view", "storages"));

        storagesView.addChild(
                new ScrollerView()
                        .addScrollViewChild(
                                storageTreeList
                                        .setOnSelectedChanged(nodes -> {
                                            if (nodes.size() != 1) return;
                                            var node = nodes.iterator().next();
                                            if (node.getContent() == null) return;
                                            setCurrentStorage(node.getContent(), node.isClient());
                                        })
                        )
                        .layout(layout -> {
                            layout.widthPercent(100);
                            layout.heightPercent(100);
                        })
        );

        return storagesView;
    }

    private View createCurrentStorageView() {
        var contentView = new View();
        contentView.setDynamicName(() -> translation("view", "content"));
        contentView.addChild(storageViewer);
        return contentView;
    }

    private @NotNull View createLoadersView() {
        var loadersView = new View();
        loadersView.setDynamicName(() -> translation("view", "loaders"));
        loadersView.addChild(loadersViewer);
        return loadersView;
    }

    public void putServerMetadata(StorageKey<?> key, StorageMetadata metadata) {
        serverStoragesMetadata.put(key, metadata);
        storageTreeList.setServerData(serverStoragesMetadata);
    }

    public void putClientMetadata(StorageKey<?> key, StorageMetadata metadata) {
        clientStoragesMetadata.put(key, metadata);
        storageTreeList.setClientData(clientStoragesMetadata);
    }

    protected void setCurrentStorage(StorageKey<?> key, boolean isClient) {
        StorageMetadata metadata = (isClient ? clientStoragesMetadata : serverStoragesMetadata).get(key);

        storageViewer.setSelectedStorage(key, metadata);
        loadersViewer.setSelectedStorage(key, metadata);
    }

    @Contract(" -> new")
    public static @NotNull ModularUI createModularUI() {
        return ModularUI.of(UI.of(new StorageEditor(), StylesheetManager.ORE_MERGED));
    }

    @RPCPacket("onServerMetadataUpdate")
    public static void onServerMetadataUpdate(@NotNull RPCSender sender, StorageKey<?> key, StorageMetadata metadata) {
        if (sender.isRemote()) return;
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) return;
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageEditor storageEditor)) return;

        storageEditor.putServerMetadata(key, metadata);
    }
}