package net.aros.engine.client.camera;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public record CameraWaypoint(@NotNull Vec3 position, float yaw, float pitch, float roll) {
    public CameraWaypoint {
        Objects.requireNonNull(position, "position");
    }

    public static @NotNull CameraWaypoint of(@NotNull Vec3 position, float yaw, float pitch) {
        return new CameraWaypoint(position, yaw, pitch, 0.0F);
    }

    public @NotNull CameraState state() {
        return new CameraState(position, new Vector3f(pitch, yaw, roll));
    }
}
