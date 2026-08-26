package net.aros.engine.client.camera;

import net.aros.engine.client.api.camera.CameraMode;
import net.aros.engine.client.api.camera.CameraRenderContext;
import net.aros.engine.client.api.camera.CameraState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record DummyCameraMode(@NotNull CameraState state) implements CameraMode {
    public DummyCameraMode {
        Objects.requireNonNull(state, "state");
    }

    @Override
    public @NotNull CameraState render(@NotNull CameraRenderContext context) {
        return state;
    }
}
