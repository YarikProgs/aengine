package net.aros.engine.client.api.camera.rotation;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public sealed interface RotationMode permits FixedRotationMode, InheritEntityRotationMode, LookAtTargetRotationMode {
    static RotationMode fixed() {
        return FixedRotationMode.INSTANCE;
    }

    @Contract("_, _ -> new")
    static @NotNull RotationMode lookAtTarget(float speed, boolean allowLookBehind) {
        return new LookAtTargetRotationMode(speed, allowLookBehind);
    }

    @Contract("_ -> new")
    static @NotNull RotationMode inheritEntity(float speed) {
        return new InheritEntityRotationMode(speed);
    }

    Vector3f interpolate(InterpolationContext context);

    Vector3f getRotation(RotationPosContext context);

    record InterpolationContext(@NotNull Vector3f fromRotation, @NotNull Vector3f toRotation) {
        public InterpolationContext {
            Objects.requireNonNull(fromRotation, "fromRotation");
            Objects.requireNonNull(toRotation, "toRotation");
        }
    }

    record RotationPosContext(Vec3 position, Vec3 anchor, float partialTick,
                              float currentYaw, float currentPitch, float entityYaw, float entityPitch) {
    }
}
