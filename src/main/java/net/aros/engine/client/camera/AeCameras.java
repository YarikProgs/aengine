package net.aros.engine.client.camera;

import net.aros.engine.client.camera.mode.CameraMode;
import net.aros.engine.client.camera.mode.DummyCameraMode;
import net.aros.engine.client.camera.mode.EntityCameraMode;
import net.aros.engine.client.camera.mode.MovingCameraMode;
import net.aros.engine.client.camera.mode.settings.EntityCameraSettings;
import net.aros.engine.client.camera.mode.settings.MovingCameraSettings;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AeCameras {
    private static final ClientCameraController CONTROLLER = new ClientCameraController();

    private AeCameras() {
    }

    static {
        NeoForge.EVENT_BUS.addListener(AeCameras::tick);
        NeoForge.EVENT_BUS.addListener(AeCameras::onUnload);
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

    private static void tick(@NotNull ClientTickEvent.Post event) {
        CONTROLLER.tick();
    }

    private static void onUnload(LevelEvent.@NotNull Unload event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getLevel() instanceof ClientLevel && event.getLevel() == minecraft.level) {
            AeCameras.reset();
        }
    }
}
