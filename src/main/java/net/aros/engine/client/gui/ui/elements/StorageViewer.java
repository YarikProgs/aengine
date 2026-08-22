package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.api.ui.StorageUIProviders;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.client.gui.ui.util.AeColors;
import net.aros.engine.client.gui.ui.util.StorageDependentUIElement;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.StorageEntry;
import net.aros.engine.common.api.storage.metadata.LoadStatus;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Set;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageViewer extends StorageDependentUIElement {
    public static final IGuiTexture CHEST = SpriteTexture
            .of(ArosEngine.id("textures/gui/sprites/widget/chest.png"))
            .setSprite(0, 0, 128, 128);

    public static final Component STATUS = translation("text", "status").append(": ");
    public static final Component LOAD_TIME = translation("text", "load_time").append(": ");
    public static final Component ENTRIES = translation("text", "entries").append(": ");

    private final UIElement content = new UIElement();
    private final UIElement emptyContent = new EmptyStateElement()
            .setText(translation("text", "no_storage"))
            .setImage(StorageViewer.CHEST);
    private final Label storageNameLabel = new Label();
    private final Label storageStatusLabel = new Label();
    private final Label storageLoadTimeLabel = new Label();
    private final Label storageEntriesLabel = new Label();
    private final Button reloadStoragesButton = new Button();
    private final ScrollerView scrollerView = new ScrollerView();

    public StorageViewer(
            StorageUI.StorageInfo serverInfo,
            StorageUI.StorageInfo clientInfo
    ) {
        super(serverInfo, clientInfo);

        int maxComponentWidth = Math.max(
                Math.max(Minecraft.getInstance().font.width(STATUS), Minecraft.getInstance().font.width(LOAD_TIME)),
                Minecraft.getInstance().font.width(ENTRIES)
        );
        getLayout().flex(1);
        content
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.heightPercent(100);
                })
                .addChildren(
                        new UIElement()
                                .layout(layout -> layout.flexDirection(FlexDirection.ROW))
                                .addClass("preview_bg")
                                .addChildren(
                                        storageNameLabel // Storage name
                                                .textStyle(textStyle -> textStyle.textAlignVertical(Vertical.CENTER))
                                                .layout(layout -> {
                                                    layout.flex(1);
                                                    layout.heightPercent(100);
                                                }),
                                        reloadStoragesButton
                                                .setText(translation("text", "reload_storage"))
                                                .addPostIcon(Icons.REPLAY)
                                                .setOnClick(this::doReload)
                                                .addClass("__white_icon__")
                                ),
                        new UIElement()
                                .layout(l -> l.flexDirection(FlexDirection.ROW))
                                .addClass("preview_bg")
                                .layout(layout -> layout.marginTop(-2).marginBottom(2))
                                .addChildren(
                                        new UIElement()
                                                .layout(layout -> layout.minWidth(maxComponentWidth + 5 * 2))
                                                .addChildren(
                                                        new Label().setText(STATUS)
                                                                .layout(l -> l.marginBottom(4)),
                                                        new Label().setText(LOAD_TIME)
                                                                .layout(l -> l.marginBottom(4)),
                                                        new Label().setText(ENTRIES)
                                                ),
                                        new UIElement()
                                                .layout(layout -> layout.flex(1))
                                                .addChildren(
                                                        storageStatusLabel.setText("").layout(l -> l.marginBottom(4)),
                                                        storageLoadTimeLabel.setText("").layout(l -> l.marginBottom(4)),
                                                        storageEntriesLabel.setText("0")
                                                )
                                ),
                        new UIElement()
                                .layout(layout -> layout.flex(1))
                                .addChildren(
                                        new UIElement().layout(layout -> layout.flexDirection(FlexDirection.ROW)).addChildren(
                                                new Label().setText(translation("text", "loaded_elements").append(": "))
                                                        .layout(l -> l.flex(1f))
                                        ).addClass("preview_bg"),
                                        new UIElement()
                                                .layout(layout -> layout.flex(1).marginTop(-2))
                                                .addClass("preview_bg")
                                                .addChild(
                                                        scrollerView
                                                                .viewPort(port -> {
                                                                    port.getStyle().backgroundTexture(IGuiTexture.EMPTY);
                                                                    port.getLayout().paddingAll(0);
                                                                    port.getLayout().paddingBottom(-1);
                                                                })
                                                                .layout(layout -> layout.flex(1))
                                                )
                                )
                );
        onStorageMetadataChanged(null);
    }

    private void doReload(UIEvent uiEvent) {
        reloadStoragesButton.setText(translation("text", "reloading"));
        reloadStoragesButton.setActive(false);

        // TODO: specific reload
    }

    @Override
    protected <T> void onStorageContentsChanged(Set<StorageEntry<T>> entries) {
        scrollerView.clearAllScrollViewChildren();

        if (entries == null || entries.isEmpty()) {
            storageEntriesLabel.setText(String.valueOf(0));
            return;
        }

        scrollerView.addScrollViewChildren(
                entries.stream().sorted(Comparator.comparing(StorageEntry::key))
                        .map(entry -> StorageUIProviders.getProvider(entry.value().type()).create(entry))
                        .toArray(UIElement[]::new)
        );
        storageEntriesLabel.setText(String.valueOf(entries.size()));
    }

    @Override
    protected void onStorageMetadataChanged(StorageMetadata metadata) {
        removeChild(content);
        removeChild(emptyContent);
        if (metadata == null) {
            addChild(emptyContent);
            return;
        }
        addChild(content);

        long loadTime = metadata.lastLoadTimeMs();
        LoadStatus status = metadata.loadStatus();

        storageNameLabel.setText(translation("text", "storage").append(": ").append(selectedKey.toString()));

        storageStatusLabel.setText(status.getName());
        storageStatusLabel.textStyle(textStyle -> textStyle.textColor(AeColors.getStatusColor(status)));

        storageLoadTimeLabel.setText(loadTimeToString(loadTime), false);
        storageLoadTimeLabel.textStyle(textStyle -> textStyle.textColor(AeColors.getLoadTimeColor(loadTime)));
    }

    private static String loadTimeToString(long loadTimeMs) {
        return loadTimeMs == -1 ? "-" : loadTimeMs + "ms";
    }
}
