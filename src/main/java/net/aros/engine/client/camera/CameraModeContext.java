package net.aros.engine.client.camera;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record CameraModeContext(@NotNull Minecraft minecraft, float partialTick, long activeTicks) {
    public CameraModeContext {
        Objects.requireNonNull(minecraft, "minecraft");
    }
}
