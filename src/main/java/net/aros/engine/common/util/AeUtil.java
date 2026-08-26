package net.aros.engine.common.util;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AeUtil {
    public static void requireUnitSpeed(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " must be between 0.0 and 1.0");
        }
    }

    public static double unwrapDegrees(double angle, double reference) {
        return reference + Mth.wrapDegrees(angle - reference);
    }

    public static float unwrapDegrees(float angle, float reference) {
        return reference + Mth.wrapDegrees(angle - reference);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Vector3f lerpRotationVectors(@NotNull Vector3f from, @NotNull Vector3f to, float rotationSpeed) {
        return new Vector3f(
                Mth.lerp(rotationSpeed, from.x, unwrapDegrees(to.x, from.x)),
                Mth.lerp(rotationSpeed, from.y, unwrapDegrees(to.y, from.y)),
                Mth.lerp(rotationSpeed, from.z, unwrapDegrees(to.z, from.z))
        );
    }
}
