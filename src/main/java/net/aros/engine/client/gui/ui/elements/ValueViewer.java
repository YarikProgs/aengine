package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.aros.engine.client.api.ui.ValueUIElementProviders;
import net.aros.engine.client.gui.ui.ValuesUI;
import net.aros.engine.common.api.value.Value;
import net.aros.engine.common.init.AeRegistries;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.aros.engine.common.ArosEngine.translation;

public class ValueViewer extends UIElement {
    public final UIElement content = new UIElement();
    public final UIElement emptyContent = new EmptyStateElement()
            .setText(translation("text", "no_value"))
            .setImage(StorageViewer.CHEST);
    public final Label pathLabel = new Label();
    public final Label typeLabel = new Label();
    public final UIElement valueHolder = new UIElement();

    private String selected;
    private String next;

    public ValueViewer() {
        getLayout().flex(1);

        content.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        }).addChildren(
                new UIElement()
                        .addClass("preview_bg")
                        .addChildren(pathLabel.setText("")
                                .textStyle(style -> style.textWrap(TextWrap.WRAP).adaptiveHeight(true))
                                .layout(layout -> layout.widthPercent(100))),
                new UIElement()
                        .addClass("preview_bg")
                        .layout(layout -> layout.marginTop(-2))
                        .addChildren(typeLabel.setText("")
                                .textStyle(style -> style.textWrap(TextWrap.WRAP).adaptiveHeight(true))
                                .layout(layout -> layout.widthPercent(100))),
                valueHolder
                        .addClass("preview_bg")
                        .layout(layout -> layout.marginTop(-2).widthPercent(100).flex(1))
        );
        onSet(null);
    }

    public void setSelectedValue(String path) {
        if (Objects.equals(selected, path)) return;
        next = path;
        fetchValue();
    }

    private void setNullValue() {
        pathLabel.setText("");
        typeLabel.setText("");
        valueHolder.clearAllChildren();
        addChild(emptyContent);
    }

    private <T> void onSet(@Nullable Value<T> value) {
        selected = next;

        removeChild(content);
        removeChild(emptyContent);
        if (value == null) {
            setNullValue();
            return;
        }

        addChild(content);
        pathLabel.setText(selected);
        typeLabel.setText(Objects.requireNonNull(AeRegistries.VALUE_TYPE.getKey(value.type())).toString());
        valueHolder.clearAllChildren();

        var element = ValueUIElementProviders
                .getProvider(value.type())
                .create(value, this::setValueOnServer);
        valueHolder.addChild(element);
    }

    private <T> void setValueOnServer(Value<T> newValue) {
        RPCPacketDistributor.rpcToServer("set_value", selected, newValue);
    }

    private void fetchValue() {
        RPCPacketDistributor.rpcToServer("get_value", "receive_value", next);
    }

    @RPCPacket("receive_value")
    public static void receiveValue(Value<?> value) {
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen) ||
                !(modularUIScreen.modularUI.ui.rootElement instanceof ValuesUI ui)) return;
        ui.valueViewer.onSet(value);
    }
}
