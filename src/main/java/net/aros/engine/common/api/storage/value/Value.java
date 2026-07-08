package net.aros.engine.common.api.storage.value;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Value<T> {
    public static final String TYPE_KEY = "type";
    public static final String VALUE_KEY = "value";

    private final ValueType<T> type;
    private final T value;

    Value(ValueType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public <R> DataResult<R> encode(@NotNull DynamicOps<R> ops) {
        return ops.mapBuilder()
                .add(TYPE_KEY, ValueType.CODEC.encodeStart(ops, type))
                .add(VALUE_KEY, type.valueCodec().encodeStart(ops, value))
                .build((R) null);
    }

    public static <R> DataResult<? extends Value<?>> parse(@NotNull DynamicOps<R> ops, R value) {
        return ops.getMap(value).flatMap(map -> {
            DataResult<ValueType<?>> type = ValueType.CODEC.parse(ops, map.get(TYPE_KEY));
            return type.flatMap(t -> parseValue(t, ops, map.get(VALUE_KEY)));
        });
    }

    private static <T, R> DataResult<Value<T>> parseValue(@NotNull ValueType<T> type, DynamicOps<R> ops, R value) {
        return type.valueCodec().parse(ops, value).map(type::create);
    }

    public ValueType<T> type() {
        return type;
    }

    public T value() {
        return value;
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
