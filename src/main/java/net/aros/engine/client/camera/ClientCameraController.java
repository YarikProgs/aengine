package net.aros.engine.client.camera;

import net.aros.engine.client.api.camera.CameraMode;
import net.aros.engine.client.api.camera.CameraModeContext;
import net.aros.engine.client.api.camera.CameraRenderContext;
import net.aros.engine.client.api.camera.CameraState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ClientCameraController {
    private static final Minecraft MC = Minecraft.getInstance();
    private @Nullable CameraMode mode;
    private long activeTicks;

    public void set(@NotNull CameraMode mode) {
        Minecraft minecraft = Minecraft.getInstance();
        reset();

        this.mode = mode;
        this.activeTicks = 0L;
        mode.start(new CameraModeContext(minecraft, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), activeTicks));
    }

    public void tick() {
        if (MC.isPaused()) return;
        if (MC.level == null || MC.player == null) {
            reset();
            return;
        }

        CameraMode currentMode = mode;
        if (currentMode == null) {
            return;
        }

        CameraModeContext tickContext = new CameraModeContext(MC, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), activeTicks);
        currentMode.tick(tickContext);
        activeTicks++;

        if (currentMode.isFinished(new CameraModeContext(MC, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), activeTicks))) {
            reset();
        }
    }

    public @NotNull Optional<CameraState> renderState(@NotNull Camera camera, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            reset();
            return Optional.empty();
        }

        CameraMode currentMode = mode;
        if (currentMode == null) {
            return Optional.empty();
        }

        return Optional.of(currentMode.render(new CameraRenderContext(minecraft, camera, partialTick, activeTicks)));
    }

    public void reset() {
        CameraMode currentMode = mode;
        if (currentMode == null) {
            activeTicks = 0L;
            return;
        }

        mode = null;
        currentMode.stop(new CameraModeContext(Minecraft.getInstance(), Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), activeTicks));
        activeTicks = 0L;
    }

    public boolean isActive() {
        return mode != null;
    }

    public @NotNull Optional<CameraMode> currentMode() {
        return Optional.ofNullable(mode);
    }
}
