package net.aros.engine.client.camera.mode;

import net.aros.engine.client.camera.CameraModeContext;
import net.aros.engine.client.camera.CameraRenderContext;
import net.aros.engine.client.camera.CameraState;
import org.jetbrains.annotations.NotNull;

public interface CameraMode {
    default void start(@NotNull CameraModeContext context) {
    }

    default void tick(@NotNull CameraModeContext context) {
    }

    @NotNull CameraState render(@NotNull CameraRenderContext context);

    default boolean isFinished(@NotNull CameraModeContext context) {
        return false;
    }

    default void stop(@NotNull CameraModeContext context) {
    }
}
