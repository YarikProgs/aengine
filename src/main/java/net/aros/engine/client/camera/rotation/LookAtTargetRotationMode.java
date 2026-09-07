package net.aros.engine.client.camera.rotation;

import net.aros.engine.common.util.AeUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

record LookAtTargetRotationMode(float speed, boolean allowLookBehind) implements RotationMode {
    private static final float LOOK_BEHIND_PITCH_WEIGHT = 0.35F;

    public LookAtTargetRotationMode {
        AeUtil.requireUnitSpeed(speed, "rotationSpeed");
    }

    @Contract("_ -> new")
    @Override
    public @NotNull Vector3f interpolate(@NotNull InterpolationContext context) {
        return AeUtil.lerpRotationVectors(context.fromRotation(), context.toRotation(), speed);
    }

    @Override
    public @NotNull Vector3f getRotation(RotationPosContext context) {
        return stabilizeLookAt(
                lookAt(context.position(), context.anchor()),
                context.currentYaw(),
                context.currentPitch()
        );
    }

    private static @NotNull Vector3f lookAt(@NotNull Vec3 position, @NotNull Vec3 target) {
        double dx = target.x - position.x;
        double dy = target.y - position.y;
        double dz = target.z - position.z;
        double flatDistance = Math.sqrt(dx * dx + dz * dz);
        float pitch = Mth.wrapDegrees((float) (-(Mth.atan2(dy, flatDistance) * 180.0F / (float) Math.PI)));
        float yaw = Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * 180.0F / (float) Math.PI) - 90.0F);
        return new Vector3f(pitch, yaw, 0);
    }

    private @NotNull Vector3f stabilizeLookAt(@NotNull Vector3f rotation, float referenceYaw, float referencePitch) {
        Vector3f normal = new Vector3f(AeUtil.unwrapDegrees(rotation.x, referencePitch), AeUtil.unwrapDegrees(rotation.y, referenceYaw), 0);
        if (!allowLookBehind) {
            return normal;
        }

        float behindYaw = AeUtil.unwrapDegrees(rotation.y + 180.0F, referenceYaw);
        float behindPitch = AeUtil.unwrapDegrees(180.0F - rotation.x, referencePitch);
        Vector3f behind = new Vector3f(behindPitch, behindYaw, 0);

        return lookBehindCost(behind, referenceYaw, referencePitch) < lookBehindCost(normal, referenceYaw, referencePitch) ? behind : normal;
    }

    private static float lookBehindCost(@NotNull Vector3f rotation, float referenceYaw, float referencePitch) {
        return Math.abs(rotation.y - referenceYaw) + Math.abs(rotation.x - referencePitch) * LOOK_BEHIND_PITCH_WEIGHT;
    }
}