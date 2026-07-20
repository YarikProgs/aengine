package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.common.api.storage.StorageEntry;

public interface StorageTypeUIElementFactory<T> {
    UIElement create(StorageEntry<T> element);

    final class Default implements StorageTypeUIElementFactory<Object> {
        @Override
        public UIElement create(StorageEntry<Object> element) {
            return new UIElement()
                    .layout(layout -> {
                        layout.flexDirection(FlexDirection.ROW);
                    })
                    .addChild(
                            new Label()
                                    .setText(element.key().toString())
                                    .layout(layout -> layout.flex(1))
                    );
        }
    }
}
