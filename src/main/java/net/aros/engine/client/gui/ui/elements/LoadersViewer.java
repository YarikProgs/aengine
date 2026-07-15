package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.util.AeColors;
import net.aros.engine.client.gui.util.StorageDependentUIElement;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.LoadStatus;
import net.aros.engine.common.api.storage.storage.metadata.LoaderMetadata;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static net.aros.engine.common.ArosEngine.translation;

public class LoadersViewer extends StorageDependentUIElement {
    private final UIElement content = new UIElement();
    private final UIElement emptyContent = new EmptyStorageStateElement();
    private final ScrollerView scrollerView = new ScrollerView();

    public LoadersViewer(
            Map<StorageKey<?>, StorageMetadata> serverMetadata,
            Map<StorageKey<?>, StorageMetadata> clientMetadata
    ) {
        super(serverMetadata, clientMetadata);
        getLayout().flex(1);

        content.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }).addChildren(
                new UIElement()
                        .layout(layout -> layout.widthPercent(100).flexDirection(FlexDirection.ROW))
                        .addClass("preview_bg")
                        .addChildren(
                                new Label().setText(translation("text", "key"))
                                        .layout(layout -> layout.flex(0.5f)),
                                new Label().setText(translation("text", "status"))
                                        .layout(layout -> layout.flex(0.25f)),
                                new Label().setText(translation("text", "load_time"))
                                        .layout(layout -> layout.flex(0.25f))
                        ),
                new UIElement()
                        .layout(layout -> layout.widthPercent(100).flex(1).marginTop(-2))
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
        );

        onSelectedStorageChanged(null);
    }

    @Override
    protected void onSelectedStorageChanged(SelectedStorage selectedStorage) {
        removeChild(content);
        removeChild(emptyContent);
        if (selectedStorage == null) {
            addChildren(emptyContent);
            return;
        }
        addChild(content);

        scrollerView.clearAllScrollViewChildren();
        selectedStorage.metadata().loadersMetadata().forEach((key, metadata) -> {
            scrollerView.addScrollViewChild(createScrollViewChild(key, metadata));
        });
    }

    @Contract(pure = true)
    private @NotNull UIElement createScrollViewChild(@NotNull ResourceLocation key, @NotNull LoaderMetadata metadata) {
        return new UIElement().layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.widthPercent(100);
        }).addChildren(
                new Label().setText(key.toString()).layout(layout -> layout.flex(0.5f))
                        .style(style -> {
                            LoadStatus status = metadata.loadStatus();
                            if (status == LoadStatus.FAIL) style.color(AeColors.getStatusColor(status));
                        }),
                new Label().setText(metadata.loadStatus().getName()).layout(layout -> layout.flex(0.25f))
                        .style(style -> style.color(AeColors.getStatusColor(metadata.loadStatus()))),
                new Label().setText(loadTimeToString(metadata.lastLoadTimeMs())).layout(layout -> layout.flex(0.25f))
                        .style(style -> style.color(AeColors.getLoadTimeColor(metadata.lastLoadTimeMs())))
        );
    }

    private static String loadTimeToString(long loadTimeMs) {
        return loadTimeMs == -1 ? "-" : loadTimeMs + "ms";
    }
}
