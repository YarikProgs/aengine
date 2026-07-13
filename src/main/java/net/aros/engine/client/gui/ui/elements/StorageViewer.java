package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.util.AeColors;
import net.aros.engine.client.gui.util.StorageDependent;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.metadata.LoadStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageViewer extends UIElement implements StorageDependent {
    public static final IGuiTexture CHEST = SpriteTexture
            .of(ArosEngine.id("textures/gui/sprites/widget/chest.png"))
            .setSprite(0, 0, 128, 128);

    public static final Component STATUS = translation("text", "status").append(": ");
    public static final Component LOAD_TIME = translation("text", "load_time").append(": ");
    public static final Component ENTRIES = translation("text", "entries").append(": ");

    private final UIElement content = new UIElement();
    private final UIElement emptyContent = new EmptyStorageStateElement();
    private final Label storageNameLabel = new Label();
    private final Label storageStatusLabel = new Label();
    private final Label storageLoadTimeLabel = new Label();
    private final Label storageEntriesLabel = new Label();

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
                        new UIElement()
                                .addClass("preview_bg")
                                .addChild(
                                        storageNameLabel // Storage name
                                                .textStyle(textStyle -> textStyle.textAlignVertical(Vertical.CENTER))
                                ),
                        new UIElement()
                                .layout(l -> l.flexDirection(FlexDirection.ROW))
                                .addClass("preview_bg")
                                .layout(layout -> layout.marginTop(-2).marginBottom(2))
                                .addChildren(
                                        new UIElement()
                                                .layout(layout -> {
                                                    layout.minWidth(maxComponentWidth + 5 * 2);
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
                                                })
                                                .addChildren(
                                                        storageStatusLabel.layout(l -> l.marginBottom(4)),
                                                        storageLoadTimeLabel.layout(l -> l.marginBottom(4)),
                                                        storageEntriesLabel
                                                )
                                ),
                        new UIElement()
                                .layout(layout -> layout.flex(1))
                                .addChildren(
                                        new UIElement().layout(layout -> {
                                            layout.flexDirection(FlexDirection.ROW);
                                        }).addChildren(
                                                new Label().setText(translation("text", "name"))
                                                        .layout(l -> l.flex(0.5f)),
                                                new Label().setText(translation("text", "path"))
                                                        .layout(l -> l.flex(0.5f))
                                        ).addClass("preview_bg"),
                                        new UIElement()
                                                .layout(layout -> layout.flex(1).marginTop(-2))
                                                .addClass("preview_bg")
                                                .addChild(
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
                                                                .viewPort(port -> {
                                                                    port.getStyle().backgroundTexture(IGuiTexture.EMPTY);
                                                                    port.getLayout().paddingAll(0);
                                                                    port.getLayout().paddingBottom(-1);
                                                                })
                                                                .layout(layout -> layout.flex(1))
                                                )
                                )
                );
        onSelectedStorageChanged(null);
    }

    @Override
    public void onSelectedStorageChanged(SelectedStorage selectedStorage) {
        removeChild(content);
        removeChild(emptyContent);
        if (selectedStorage == null) {
            addChild(emptyContent);
            return;
        }
        addChild(content);

        long loadTime = selectedStorage.metadata().lastLoadTimeMs();
        LoadStatus status = selectedStorage.metadata().loadStatus();

        storageNameLabel.setText(translation("text", "storage").append(": ").append(selectedStorage.key().toString()));

        storageStatusLabel.setText(status.getName());
        storageStatusLabel.textStyle(textStyle -> textStyle.textColor(AeColors.getStatusColor(status)));

        storageLoadTimeLabel.setText(loadTime + "ms", false);
        storageLoadTimeLabel.textStyle(textStyle -> textStyle.textColor(AeColors.getLoadTimeColor(loadTime)));

        storageEntriesLabel.setText(String.valueOf(0));
    }
}
