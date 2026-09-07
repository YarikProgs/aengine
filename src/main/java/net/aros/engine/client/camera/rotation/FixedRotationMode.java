package net.aros.engine.client.camera.rotation;

import org.joml.Vector3f;

record FixedRotationMode() implements RotationMode {
    public static final FixedRotationMode INSTANCE = new FixedRotationMode();

    @Override
    public Vector3f interpolate(InterpolationContext context) {
        return context.toRotation();
    }

    @Override
    public Vector3f getRotation(RotationPosContext context) {
        return new Vector3f(0, 0, 0);
    }
}