package net.aros.engine.client.camera.mode;

import net.aros.engine.client.camera.CameraRenderContext;
import net.aros.engine.client.camera.CameraState;
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
