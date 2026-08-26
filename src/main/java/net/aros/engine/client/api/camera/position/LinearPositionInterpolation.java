package net.aros.engine.client.api.camera.position;

import net.aros.engine.common.util.AeUtil;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

record LinearPositionInterpolation(double speed) implements PositionInterpolation {
    public LinearPositionInterpolation {
        AeUtil.requireUnitSpeed(speed, "speed");
    }

    @Override
    public @NotNull Vec3 interpolate(@NotNull Context context) {
        if (speed == 1) return context.toPosition();
        return context.fromPosition().lerp(context.toPosition(), speed);
    }
}
