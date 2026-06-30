package net.aros.engine.client.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.aros.engine.client.ui.EngineScreen;
import net.aros.engine.common.ArosEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AEngineKeyBinds {
    private static final KeyMapping openEngineUi = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            ArosEngine.translationKey("key", "openEngineUi"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            ArosEngine.translationKey("category", null)
    ));

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
            @Override
            public void onEndTick(Minecraft mc) {
                if (openEngineUi.consumeClick()) {
                    mc.setScreen(new EngineScreen());
                }
            }
        });
    }
}
