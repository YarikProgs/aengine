package net.aros.engine.client.api.camera.settings;

import net.aros.engine.client.api.camera.position.PositionInterpolation;
import net.aros.engine.client.api.camera.rotation.RotationMode;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public record EntityCameraSettings(
        @NotNull Vec3 offset,
        @NotNull Vector3f rotationOffset,
        @NotNull OffsetSpace offsetSpace,
        @NotNull PositionInterpolation positionInterpolation,
        @NotNull RotationMode rotationMode,
        boolean useEyePosition,
        boolean avoidBlocks,
        boolean resetWhenEntityRemoved
) {
    public static final EntityCameraSettings DEFAULT = new EntityCameraSettings(
            new Vec3(0.0D, 2.0D, -6.0D),
            new Vector3f(0, 0, 0),
            OffsetSpace.ENTITY,
            PositionInterpolation.linear(1.0D),
            RotationMode.lookAtTarget(1.0F, true),
            false,
            true,
            true
    );

    public EntityCameraSettings {
        Objects.requireNonNull(offset, "offset");
        Objects.requireNonNull(rotationOffset, "rotationOffset");
        Objects.requireNonNull(offsetSpace, "offsetSpace");
        Objects.requireNonNull(positionInterpolation, "positionInterpolator");
        Objects.requireNonNull(rotationMode, "rotationMode");
    }

    public enum OffsetSpace {
        WORLD,
        ENTITY,
        ENTITY_YAW
    }
}
