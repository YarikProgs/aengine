package net.aros.engine.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.aros.engine.client.gui.screen.EngineScreen;
import net.aros.engine.common.ArosEngine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AeKeybinds {
    private static final KeyMapping openEngineUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            ArosEngine.translationKey("key", "openEngineUi"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            ArosEngine.translationKey("category", null)
    ));

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (openEngineUI.consumeClick()) {
                mc.setScreen(new EngineScreen());
            }
        });
    }
}
