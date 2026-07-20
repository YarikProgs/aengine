package net.aros.engine.client.gui.ui;

import com.lowdragmc.lowdraglib2.editor.ui.SplittableWindow;
import com.lowdragmc.lowdraglib2.editor.ui.View;
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
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.ui.elements.LoadersViewer;
import net.aros.engine.client.gui.ui.elements.StorageTreeList;
import net.aros.engine.client.gui.ui.elements.StorageViewer;
import net.aros.engine.client.gui.util.StorageDependentUIElement;
import net.aros.engine.client.session.ClientStorageUISession;
import net.aros.engine.common.api.storage.storage.StorageEntry;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.type.ContentUpdate;
import org.appliedenergistics.yoga.YogaEdge;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageUI extends UIElement {
    private final StorageInfo clientInfo = new StorageInfo();
    private final StorageInfo serverInfo = new StorageInfo();

    public final SplittableWindow rootWindow;
    public SplittableWindow leftWindow, centerWindow;

    public final StorageTreeList storageTreeList = new StorageTreeList();
    public final StorageDependentUIElement storageViewer = new StorageViewer(serverInfo, clientInfo);
    public final StorageDependentUIElement loadersViewer = new LoadersViewer(serverInfo, clientInfo);

    public final UIElement top = new UIElement();
    public final UIElement icon = new UIElement();
    public final UIElement menuContainer = new UIElement();
    public final UIElement topPlaceholder = new UIElement();
    public final UIElement buttonContainer = new UIElement();
    public final Button closeButton = new Button();

    public StorageUI() {
        addEventListener(UIEvents.MUI_CHANGED, e -> {
            ClientStorageUISession.createBoth();
        });
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
                getModularUI().getScreen().onClose();
            }
        });
        addEventListener(UIEvents.REMOVED, e -> {
            ClientStorageUISession.closeBoth();
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
                                            setSelectedStorage(node.getContent(), node.isClient());
                                        })
                        )
                        .layout(layout -> {
                            layout.widthPercent(100);
                            layout.heightPercent(100);
                        })
        );

        return storagesView;
    }

    private @NotNull View createCurrentStorageView() {
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

    public void putMetadata(StorageKey<?> key, StorageMetadata metadata, boolean isClient) {
        if (isClient) {
            clientInfo.metadata().put(key, metadata);
            storageTreeList.setClientData(clientInfo.metadata());
        } else {
            serverInfo.metadata().put(key, metadata);
            storageTreeList.setServerData(serverInfo.metadata());
        }
    }

    public void putAllMetadata(Map<StorageKey<?>, StorageMetadata> metadataMap, boolean isClient) {
        if (isClient) {
            clientInfo.metadata().putAll(metadataMap);
            storageTreeList.setClientData(clientInfo.metadata());
        } else {
            serverInfo.metadata().putAll(metadataMap);
            storageTreeList.setServerData(serverInfo.metadata());
        }
    }

    public <T> void putContents(@NotNull ContentUpdate<T> entries, boolean isClient) {
        //noinspection unchecked,rawtypes
        (isClient ? clientInfo : serverInfo).contents().put(entries.key(), (Set) entries.entries());
    }

    public void putAllContents(@NotNull List<ContentUpdate<?>> contents, boolean isClient) {
        for (ContentUpdate<?> update : contents) {
            putContents(update, isClient);
        }
    }

    protected void setSelectedStorage(StorageKey<?> key, boolean isClient) {
        storageViewer.setSelectedStorage(key, isClient);
        loadersViewer.setSelectedStorage(key, isClient);
    }

    @Contract(" -> new")
    public static @NotNull ModularUI createModularUI() {
        return ModularUI.of(UI.of(new StorageUI(), StylesheetManager.ORE_MERGED));
    }

    public record StorageInfo(Map<StorageKey<?>, StorageMetadata> metadata,
                              Map<StorageKey<?>, Set<StorageEntry<?>>> contents) {

        StorageInfo() {
            this(new HashMap<>(), new HashMap<>());
        }
    }
}