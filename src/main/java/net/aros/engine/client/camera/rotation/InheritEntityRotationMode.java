package net.aros.engine.client.camera.rotation;

import net.aros.engine.common.util.AeUtil;
import org.joml.Vector3f;

record InheritEntityRotationMode(float speed) implements RotationMode {
    public InheritEntityRotationMode {
        AeUtil.requireUnitSpeed(speed, "rotationSpeed");
    }

    @Override
    public Vector3f interpolate(InterpolationContext context) {
        return AeUtil.lerpRotationVectors(context.fromRotation(), context.toRotation(), speed);
    }

    @Override
    public Vector3f getRotation(RotationPosContext context) {
        return new Vector3f(
                AeUtil.unwrapDegrees(context.entityPitch(), context.currentPitch()),
                AeUtil.unwrapDegrees(context.entityYaw(), context.currentYaw()),
                0
        );
    }
}