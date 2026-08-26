package net.aros.engine.client.api.camera;

import net.aros.engine.client.api.camera.settings.EntityCameraSettings;
import net.aros.engine.client.api.camera.settings.MovingCameraSettings;
import net.aros.engine.client.camera.ClientCameraController;
import net.aros.engine.client.camera.DummyCameraMode;
import net.aros.engine.client.camera.EntityCameraMode;
import net.aros.engine.client.camera.MovingCameraMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AeCameras {
    private static final ClientCameraController CONTROLLER = new ClientCameraController();

    private AeCameras() {
    }

    public static void set(@NotNull CameraMode mode) {
        CONTROLLER.set(Objects.requireNonNull(mode, "mode"));
    }

    public static void setDummy(@NotNull Vec3 position, float yaw, float pitch) {
        setDummy(CameraState.of(position, yaw, pitch));
    }

    public static void setDummy(@NotNull CameraState state) {
        set(new DummyCameraMode(state));
    }

    public static void setEntity(@NotNull Entity entity) {
        setEntity(entity, EntityCameraSettings.DEFAULT);
    }

    public static void setEntity(@NotNull Entity entity, @NotNull EntityCameraSettings settings) {
        set(new EntityCameraMode(entity, settings));
    }

    public static void setMoving(@NotNull List<CameraWaypoint> waypoints) {
        setMoving(waypoints, MovingCameraSettings.DEFAULT);
    }

    public static void setMoving(@NotNull List<CameraWaypoint> waypoints, @NotNull MovingCameraSettings settings) {
        set(new MovingCameraMode(waypoints, settings));
    }

    public static void reset() {
        CONTROLLER.reset();
    }

    public static boolean isActive() {
        return CONTROLLER.isActive();
    }

    public static @NotNull Optional<CameraMode> currentMode() {
        return CONTROLLER.currentMode();
    }

    public static @NotNull Optional<CameraState> renderState(@NotNull Camera camera, float partialTick) {
        return CONTROLLER.renderState(camera, partialTick);
    }

    public static void tick(@NotNull Minecraft minecraft) {
        CONTROLLER.tick(minecraft);
    }

}
