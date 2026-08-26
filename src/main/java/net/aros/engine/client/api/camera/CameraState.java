package net.aros.engine.client.api.camera;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public record CameraState(@NotNull Vec3 position, @NotNull Vector3f rotation) {
    public CameraState {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(position, "rotation");
    }

    public static @NotNull CameraState of(@NotNull Vec3 position, float yaw, float pitch) {
        return of(position, yaw, pitch, 0.0F);
    }

    public static @NotNull CameraState of(@NotNull Vec3 position, float yaw, float pitch, float roll) {
        return new CameraState(position, new Vector3f(pitch, yaw, roll));
    }

    public static @NotNull CameraState from(@NotNull Camera camera) {
        return new CameraState(camera.getPosition(), new Vector3f(camera.getXRot(), camera.getYRot(), camera.getRoll()));
    }

    public static @NotNull CameraState lerp(@NotNull CameraState from, @NotNull CameraState to, double delta) {
        float progress = (float) Mth.clamp(delta, 0.0D, 1.0D);
        return new CameraState(
                new Vec3(
                        Mth.lerp(progress, from.position.x, to.position.x),
                        Mth.lerp(progress, from.position.y, to.position.y),
                        Mth.lerp(progress, from.position.z, to.position.z)
                ),
                new Vector3f(
                        Mth.rotLerp(progress, from.rotation().x, to.rotation().x),
                        Mth.rotLerp(progress, from.rotation().y, to.rotation().y),
                        Mth.rotLerp(progress, from.rotation().z, to.rotation().z)
                )
        );
    }
}
