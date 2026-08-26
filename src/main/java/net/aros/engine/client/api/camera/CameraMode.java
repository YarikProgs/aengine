package net.aros.engine.client.api.camera;

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
