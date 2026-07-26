package net.aros.engine.client.api.ui;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.aros.engine.common.api.value.Value;
import net.aros.engine.common.api.value.ValueType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ValuesUIProviders {
    private static final Provider<?> DEFAULT = (value, onChanged) -> new UIElement();
    private static final Map<ValueType<?>, Provider<?>> PROVIDERS = new HashMap<>();

    public static <T> void register(ValueType<?> type, Provider<T> provider) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(provider);
        PROVIDERS.put(type, provider);
    }

    public static <T> Provider<T> getProvider(ValueType<T> type) {
        //noinspection unchecked
        return (Provider<T>) PROVIDERS.getOrDefault(type, DEFAULT);
    }

    public interface Provider<T> {
        UIElement create(Value<T> value, Consumer<Value<T>> onChanged);
    }
}
