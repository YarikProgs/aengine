package net.aros.engine.client.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record CameraRenderContext(@NotNull Minecraft minecraft, @NotNull Camera camera, float partialTick, long activeTicks) {
    public CameraRenderContext {
        Objects.requireNonNull(minecraft, "minecraft");
        Objects.requireNonNull(camera, "camera");
    }

    public double elapsedTicks() {
        return (double) activeTicks + partialTick;
    }
}
