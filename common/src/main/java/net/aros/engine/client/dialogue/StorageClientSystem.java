package net.aros.engine.client.dialogue;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import net.aros.engine.client.systems.UiSystem;
import net.aros.engine.client.ui.StorageUi;
import net.aros.engine.common.ArosEngine;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class StorageClientSystem implements UiSystem {
    @Override
    public void onUiRegister(@NotNull Context ctx) {
        ctx.register(ArosEngine.id("storage"), () -> new ModularUIScreen(StorageUi.createModularUI(), Component.empty()));
        ctx.register(ArosEngine.id("values"), () -> null);
    }
}
