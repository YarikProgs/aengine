package net.aros.engine.common.api.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Objects;

public class Value<T> {
    public static final Codec<Value<?>> CODEC = ValueType.CODEC
            .dispatch(Value::type, Value::valueCodecFromType);

    private final ValueType<T> type;

    private final T value;

    Value(ValueType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public ValueType<T> type() {
        return type;
    }

    public T value() {
        return value;
    }

    private static <T> MapCodec<Value<T>> valueCodecFromType(ValueType<T> type) {
        return type.valueCodec().xmap(type::create, Value::value).fieldOf("value");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value1 = (Value<?>) o;
        return Objects.equals(type, value1.type) && Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Value{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
