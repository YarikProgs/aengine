package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.OreSprites;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.LoadStatus;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageViewer extends UIElement {
    public static final IGuiTexture CHEST = SpriteTexture
            .of(ArosEngine.id("textures/gui/sprites/widget/chest.png"))
            .setSprite(0, 0, 128, 128);

    // TODO: 08.07.2026 separate class for colors?
    public static final int SOFT_GREEN_COLOR = 0xFFa8fca4;
    public static final int SOFT_YELLOW_COLOR = 0xFFf3fca4;
    public static final int SOFT_RED_COLOR = 0xFFfca4a4;

    // TODO: 08.07.2026 config
    public static final int OK_TIME_THRESHOLD_MS = 2000;
    public static final int WARN_TIME_THRESHOLD_MS = 10000;

    public static final Component STATUS = translation("text", "status");
    public static final Component LOAD_TIME = translation("text", "load_time");
    public static final Component ENTRIES = translation("text", "entries");

    private final UIElement content = new UIElement();
    private final UIElement emptyContent = new UIElement();
    private final Label storageNameLabel = new Label();
    private final Label storageStatusLabel = new Label();
    private final Label storageLoadTimeLabel = new Label();
    private final Label storageEntriesLabel = new Label();

    private SelectedStorage<?> selectedStorage;

    public StorageViewer() {
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
                        storageNameLabel // Storage name
                                .textStyle(textStyle -> {
                                    textStyle.textAlignVertical(Vertical.CENTER);
                                })
                                .layout(layout -> {
                                    layout.widthPercent(100);
                                    layout.height(20);
                                    layout.paddingLeft(5);
                                })
                                .style(style -> {
                                    style.backgroundTexture(OreSprites.BORDER_5);
                                }),
                        new UIElement()
                                .layout(l -> l.flexDirection(FlexDirection.ROW))
                                .style(style -> style.backgroundTexture(OreSprites.BORDER_5))
                                .addChildren(
                                        new UIElement()
                                                .layout(layout -> {
                                                    layout.minWidth(maxComponentWidth + 5 * 2);
                                                    layout.paddingAll(5);
                                                })
                                                .addChildren(
                                                        new Label().setText(STATUS)
                                                                .layout(l -> l.marginBottom(4)),
                                                        new Label().setText(LOAD_TIME)
                                                                .layout(l -> l.marginBottom(4)),
                                                        new Label().setText(ENTRIES)
                                                ),
                                        new UIElement()
                                                .layout(layout -> {
                                                    layout.flex(1);
                                                    layout.paddingAll(5);
                                                    layout.paddingLeft(0);
                                                })
                                                .addChildren(
                                                        storageStatusLabel.layout(l -> l.marginBottom(4)),
                                                        storageLoadTimeLabel.layout(l -> l.marginBottom(4)),
                                                        storageEntriesLabel
                                                )
                                ),
                        new UIElement()
                                .layout(layout -> {
                                    layout.flex(1);
                                })
                                .style(style -> {
                                    style.backgroundTexture(OreSprites.BORDER_5);
                                })
                                .addChildren(
                                        new UIElement().layout(layout -> {
                                            layout.flexDirection(FlexDirection.ROW);
                                            layout.paddingAll(5);
                                            layout.paddingBottom(0);
                                        }).addChildren(
                                                new Label().setText(translation("text", "name"))
                                                        .layout(l -> l.flex(0.5f)),
                                                new Label().setText(translation("text", "path"))
                                                        .layout(l -> l.flex(0.5f))
                                        ),
                                        new ScrollerView()
                                                .addScrollViewChildren(
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A"),
                                                        new Label().setText("A")
                                                )
                                                .viewContainer(view -> {
                                                    view.getStyle().backgroundTexture(null);
                                                })
                                                .verticalContainer(vcon -> {
                                                    vcon.getStyle().backgroundTexture(null);
                                                })
                                                .viewPort(port -> {
                                                    port.getLayout().paddingAll(5);
                                                    port.getLayout().paddingTop(0);
                                                    port.getStyle().backgroundTexture(IGuiTexture.EMPTY);
                                                })
                                                .style(style -> {
                                                    style.backgroundTexture(null);
                                                })
                                                .layout(l -> {
                                                    l.flex(1);
                                                })
                                )
                );

        emptyContent
                .layout(layout -> {
                    layout.widthPercent(100);
                    layout.heightPercent(100);
                })
                .addChildren(
                        new UIElement()
                                .layout(layout -> {
                                    layout.alignSelf(AlignItems.CENTER);
                                    layout.heightPercent(50);
                                    layout.setAspectRatio(1);
                                })
                                .style(style -> {
                                    style.backgroundTexture(CHEST);
                                    style.opacity(1f);
                                }),
                        new Label()
                                .setText(translation("text", "no_storage"))
                                .textStyle(textStyle -> {
                                    textStyle.textAlignHorizontal(Horizontal.CENTER);
                                    textStyle.textShadow(false);
                                })
                                .layout(l -> {
                                    l.marginTop(10);
                                    l.widthPercent(100);
                                    l.heightPercent(50);
                                })
                                .style(style -> {
                                    style.color(0xff9b9b9b);
                                })
                );
        onSelectedStorageChanged();
    }

    public void setCurrentStorage(StorageKey<?> key, StorageMetadata storageMetadata) {
        selectedStorage = new SelectedStorage<>(key, storageMetadata);
        onSelectedStorageChanged();
    }

    private void onSelectedStorageChanged() {
        removeChild(content);
        removeChild(emptyContent);
        if (selectedStorage == null) {
            addChild(emptyContent);
            return;
        }
        addChild(content);

        long loadTime = selectedStorage.metadata.lastLoadTimeMs();
        LoadStatus status = selectedStorage.metadata.loadStatus();

        storageNameLabel.setText(translation("text", "storage", selectedStorage.key.toString()));

        storageStatusLabel.setText(translation("load_status", status.name().toLowerCase()));
        storageStatusLabel.textStyle(textStyle -> textStyle.textColor(determineStatusColor(status)));

        storageLoadTimeLabel.setText(loadTime + "ms", false);
        storageLoadTimeLabel.textStyle(textStyle -> textStyle.textColor(determineLoadTimeColor(loadTime)));

        storageEntriesLabel.setText(String.valueOf(0));
    }

    @Contract(pure = true)
    private int determineStatusColor(@NotNull LoadStatus status) {
        return switch (status) {
            case OK -> SOFT_GREEN_COLOR;
            case RELOADING -> 0xFFFFFFFF;
            case FAIL -> SOFT_RED_COLOR;
        };
    }

    private int determineLoadTimeColor(long loadTimeMs) {
        if (loadTimeMs < OK_TIME_THRESHOLD_MS)
            return SOFT_GREEN_COLOR;
        if (loadTimeMs < WARN_TIME_THRESHOLD_MS)
            return SOFT_YELLOW_COLOR;
        return SOFT_RED_COLOR;
    }

    record SelectedStorage<T>(StorageKey<T> key, StorageMetadata metadata) {
    }
}
