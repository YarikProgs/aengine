package net.aros.engine.client.camera;

import net.aros.engine.client.api.camera.CameraMode;
import net.aros.engine.client.api.camera.CameraModeContext;
import net.aros.engine.client.api.camera.CameraRenderContext;
import net.aros.engine.client.api.camera.CameraState;
import net.aros.engine.client.api.camera.CameraWaypoint;
import net.aros.engine.client.api.camera.settings.MovingCameraSettings;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class MovingCameraMode implements CameraMode {
    private final List<CameraWaypoint> waypoints;
    private final MovingCameraSettings settings;
    private final int[] segmentTicks;
    private final int pathDurationTicks;

    private CameraState moveToFirstStart;

    public MovingCameraMode(@NotNull List<CameraWaypoint> waypoints, @NotNull MovingCameraSettings settings) {
        if (waypoints.isEmpty()) {
            throw new IllegalArgumentException("Moving camera needs at least one waypoint");
        }
        this.waypoints = List.copyOf(waypoints);
        this.settings = Objects.requireNonNull(settings, "settings");
        this.segmentTicks = settings.segmentTicks(this.waypoints);
        this.pathDurationTicks = sum(segmentTicks);
    }

    @Override
    public @NotNull CameraState render(@NotNull CameraRenderContext context) {
        double elapsed = context.elapsedTicks();
        if (settings.moveToFirstWaypointTicks() > 0) {
            if (moveToFirstStart == null) {
                moveToFirstStart = CameraState.from(context.camera());
            }
            if (elapsed < settings.moveToFirstWaypointTicks()) {
                return CameraState.lerp(moveToFirstStart, waypoints.getFirst().state(), progress(elapsed, settings.moveToFirstWaypointTicks()));
            }
            elapsed -= settings.moveToFirstWaypointTicks();
        }

        if (elapsed < pathDurationTicks) {
            return renderPath(elapsed);
        }

        elapsed -= pathDurationTicks;
        CameraState lastWaypoint = waypoints.getLast().state();
        if (settings.resetOnFinish() && settings.resetTicks() > 0 && elapsed < settings.resetTicks()) {
            return CameraState.lerp(lastWaypoint, CameraState.from(context.camera()), progress(elapsed, settings.resetTicks()));
        }

        return settings.resetOnFinish() ? CameraState.from(context.camera()) : lastWaypoint;
    }

    @Override
    public boolean isFinished(@NotNull CameraModeContext context) {
        return settings.resetOnFinish() && context.activeTicks() > totalDurationTicks();
    }

    private @NotNull CameraState renderPath(double elapsed) {
        if (waypoints.size() == 1) {
            return waypoints.getFirst().state();
        }

        int segment = 0;
        double segmentStart = 0.0D;
        while (segment < segmentTicks.length - 1 && elapsed >= segmentStart + segmentTicks[segment]) {
            segmentStart += segmentTicks[segment];
            segment++;
        }

        return CameraState.lerp(
                waypoints.get(segment).state(),
                waypoints.get(segment + 1).state(),
                progress(elapsed - segmentStart, segmentTicks[segment])
        );
    }

    private double progress(double elapsed, int durationTicks) {
        double progress = Mth.clamp(elapsed / durationTicks, 0.0D, 1.0D);
        if (settings.smoothStep()) {
            progress = Mth.smoothstep(progress);
        }
        return progress;
    }

    private int totalDurationTicks() {
        return settings.moveToFirstWaypointTicks() + pathDurationTicks + (settings.resetOnFinish() ? settings.resetTicks() : 0);
    }

    private static int sum(int[] values) {
        int result = 0;
        for (int value : values) {
            result += value;
        }
        return result;
    }
}
