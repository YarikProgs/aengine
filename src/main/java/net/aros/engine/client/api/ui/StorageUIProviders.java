package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.common.api.storage.StorageEntry;
import net.aros.engine.common.api.value.ValueType;

import java.util.HashMap;
import java.util.Map;

public class StorageUIProviders {
    private static final Factory<?> DEFAULT = new Default();
    private static final Map<ValueType<?>, Factory<?>> PROVIDERS = new HashMap<>();

    public static <T> void register(ValueType<T> type, Factory<T> factory) {
        PROVIDERS.put(type, factory);
    }

    public static <T> Factory<T> getProvider(ValueType<T> type) {
        //noinspection unchecked
        return (Factory<T>) PROVIDERS.getOrDefault(type, DEFAULT);
    }

    public interface Factory<T> {
        UIElement create(StorageEntry<T> element);
    }

    private static final class Default implements Factory<Object> {
        @Override
        public UIElement create(StorageEntry<Object> element) {
            return new UIElement()
                    .layout(layout -> layout.flexDirection(FlexDirection.ROW))
                    .addChild(
                            new Label()
                                    .setText(element.key().toString())
                                    .layout(layout -> layout.flex(1))
                    );
        }
    }
}
