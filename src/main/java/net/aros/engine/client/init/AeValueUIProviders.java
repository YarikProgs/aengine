package net.aros.engine.client.init;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aros.engine.client.api.ui.ValueUIElementProviders;
import net.aros.engine.client.api.ui.ValueUIElementProviders.Provider;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.value.Value;
import net.aros.engine.common.api.value.ValueType;
import net.aros.engine.common.init.AeValueTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.aros.engine.common.ArosEngine.translation;

public class AeValueUIProviders {
    public static void init() {
        ValueUIElementProviders.register(AeValueTypes.BOOL, new BoolProvider());
        ValueUIElementProviders.register(AeValueTypes.BYTE, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyByte(Byte.MIN_VALUE, Byte.MAX_VALUE),
                Byte::parseByte,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.SHORT, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyShort(Short.MIN_VALUE, Short.MAX_VALUE),
                Short::parseShort,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.INT, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyInt(Integer.MIN_VALUE, Integer.MAX_VALUE),
                Integer::parseInt,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.LONG, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyLong(Long.MIN_VALUE, Long.MAX_VALUE),
                Long::parseLong,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.FLOAT, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyFloat(Float.MIN_VALUE, Float.MAX_VALUE),
                Float::parseFloat,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.DOUBLE, new ProviderWithTextField<>(
                field -> field.setNumbersOnlyDouble(Double.MIN_VALUE, Double.MAX_VALUE),
                Double::parseDouble,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.STRING, new ProviderWithTextField<>(
                UnaryOperator.identity(),
                Function.identity(),
                Function.identity()
        ));
        ValueUIElementProviders.register(AeValueTypes.RESOURCE_LOCATION, new ProviderWithTextField<>(
                TextField::setResourceLocationOnly,
                ResourceLocation::parse,
                Object::toString
        ));
        ValueUIElementProviders.register(AeValueTypes.COMPOUND_TAG, new ProviderWithTextField<>(
                TextField::setCompoundTagOnly,
                text -> {
                    try {
                        return TagParser.parseTag(text);
                    } catch (CommandSyntaxException e) {
                        ArosEngine.LOGGER.error("Somehow got invalid nbt, even after validation");
                        return new CompoundTag();
                    }
                },
                tag -> new StringTagVisitor().visit(tag)
        ));
    }

    private static class ProviderWithTextField<T> implements Provider<T> {
        private final UnaryOperator<TextField> configurator;
        private final Function<String, T> parser;
        private final Function<T, String> encoder;

        public ProviderWithTextField(UnaryOperator<TextField> configurator, Function<String, T> parser, Function<T, String> encoder) {
            this.configurator = configurator;
            this.parser = parser;
            this.encoder = encoder;
        }

        @Override
        public UIElement create(Value<T> value, Consumer<Value<T>> onChanged) {
            Button button = new Button();
            button.setActive(false);
            TextField field = configurator.apply(new TextField()).setText(encoder.apply(value.get()));
            return new UIElement()
                    .layout(layout -> layout.widthPercent(100).flex(1))
                    .addChildren(
                            field
                                    .setTextResponder(__ -> button.setActive(true))
                                    .layout(layout -> layout.widthPercent(100)),
                            button
                                    .setText(translation("text", "send"))
                                    .textStyle(textStyle -> textStyle.textAlignHorizontal(Horizontal.LEFT))
                                    .addPreIcon(Icons.SAVE)
                                    .setOnClick(e -> onClick(value.type(), button, field, onChanged))
                                    .addClass("__white_icon__")
                                    .layout(layout -> layout.widthPercent(33))
                    );
        }

        private void onClick(ValueType<T> type, Button button, TextField field, Consumer<Value<T>> onChanged) {
            if (!field.isError()) {
                button.setActive(false);
                onChanged.accept(type.create(parser.apply(field.getValue())));
            }
        }
    }

    private static class BoolProvider implements Provider<Boolean> {
        @Override
        public UIElement create(Value<Boolean> value, Consumer<Value<Boolean>> onChanged) {
            return new Toggle()
                    .setOn(value.get())
                    .setOnToggleChanged(on -> onChanged.accept(AeValueTypes.BOOL.create(on)))
                    .setText("");
        }
    }
}
