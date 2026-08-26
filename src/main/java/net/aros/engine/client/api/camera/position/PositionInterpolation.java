package net.aros.engine.client.api.camera.position;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed interface PositionInterpolation permits LinearPositionInterpolation {
    static @NotNull PositionInterpolation linear(double speed) {
        return new LinearPositionInterpolation(speed);
    }

    static @NotNull PositionInterpolation linear() {
        return linear(1);
    }

    @NotNull Vec3 interpolate(@NotNull Context context);

    record Context(@NotNull Vec3 fromPosition, @NotNull Vec3 toPosition, @NotNull Vec3 pivot) {
        public Context {
            Objects.requireNonNull(fromPosition, "fromRotation");
            Objects.requireNonNull(toPosition, "toRotation");
            Objects.requireNonNull(pivot, "pivot");
        }
    }
}
