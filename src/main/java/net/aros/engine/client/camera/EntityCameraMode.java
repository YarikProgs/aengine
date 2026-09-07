package net.aros.engine.client.camera;

import net.aros.engine.client.api.camera.CameraMode;
import net.aros.engine.client.api.camera.CameraModeContext;
import net.aros.engine.client.api.camera.CameraRenderContext;
import net.aros.engine.client.api.camera.CameraState;
import net.aros.engine.client.api.camera.position.PositionInterpolation;
import net.aros.engine.client.api.camera.rotation.RotationMode;
import net.aros.engine.client.api.camera.settings.EntityCameraSettings;
import net.aros.engine.common.util.AeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public final class EntityCameraMode implements CameraMode {
    private final Entity entity;
    private final EntityCameraSettings settings;

    private CameraState previous;
    private CameraState current;

    public EntityCameraMode(@NotNull Entity entity, @NotNull EntityCameraSettings settings) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    @Override
    public void start(@NotNull CameraModeContext context) {
        current = target(currentPartialTick(context.minecraft())).state();
        previous = current;
    }

    @Override
    public void tick(@NotNull CameraModeContext context) {
        if (shouldReset()) {
            return;
        }

        TargetState target = target(context.partialTick());
        if (current == null) {
            current = target.state();
            previous = current;
            return;
        }

        previous = current;
        current = blendState(current, target);
    }

    @Override
    public @NotNull CameraState render(@NotNull CameraRenderContext context) {
        if (current == null || previous == null) {
            current = target(context.partialTick()).state();
            previous = current;
            return current;
        }

        return blendState(CameraState.lerp(previous, current, context.partialTick()), target(context.partialTick()));
    }

    @Override
    public boolean isFinished(@NotNull CameraModeContext context) {
        return shouldReset();
    }

    private boolean shouldReset() {
        return settings.resetWhenEntityRemoved() && entity.isRemoved();
    }

    private @NotNull TargetState target(float partialTick) {
        Vec3 anchor = settings.useEyePosition() ? entity.getEyePosition(partialTick) : entity.getPosition(partialTick);
        Vec3 position = applyOffset(anchor, partialTick);
        if (settings.avoidBlocks()) {
            position = avoidBlocks(anchor, position);
        }

        float pitch = currentPitch(partialTick);
        float yaw = currentYaw(partialTick);

        Vector3f rotation = settings.rotationMode().getRotation(new RotationMode.RotationPosContext(
                position, anchor, partialTick, yaw, pitch,
                entity.getViewYRot(partialTick), entity.getViewXRot(partialTick)
        ));
        return new TargetState(
                new CameraState(
                        position,
                        new Vector3f(
                                AeUtil.unwrapDegrees(rotation.x + settings.rotationOffset().x, pitch),
                                AeUtil.unwrapDegrees(rotation.y + settings.rotationOffset().y, yaw),
                                settings.rotationOffset().z
                        )
                ),
                anchor
        );
    }

    private @NotNull Vec3 applyOffset(@NotNull Vec3 anchor, float partialTick) {
        return applyOffset(anchor, partialTick, settings.offsetSpace());
    }

    private @NotNull Vec3 applyOffset(@NotNull Vec3 anchor, float partialTick, @NotNull EntityCameraSettings.OffsetSpace offsetSpace) {
        Vec3 offset = settings.offset();
        if (offsetSpace == EntityCameraSettings.OffsetSpace.WORLD) {
            return anchor.add(offset);
        }

        float yaw = entity.getViewYRot(partialTick);
        float pitch = offsetSpace == EntityCameraSettings.OffsetSpace.ENTITY_YAW ? 0.0F : entity.getViewXRot(partialTick);
        Vec3 forward = entity.calculateViewVector(pitch, yaw).normalize();
        Vec3 up = entity.calculateViewVector(pitch - 90.0F, yaw).normalize();
        Vec3 right = up.cross(forward).normalize();
        return anchor.add(right.scale(offset.x)).add(up.scale(offset.y)).add(forward.scale(offset.z));
    }

    private @NotNull Vec3 avoidBlocks(@NotNull Vec3 anchor, @NotNull Vec3 position) {
        HitResult hit = entity.level().clip(new ClipContext(anchor, position, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
        return hit.getType() == HitResult.Type.MISS ? position : hit.getLocation();
    }

    private float currentYaw(float partialTick) {
        if (settings.offsetSpace() == EntityCameraSettings.OffsetSpace.WORLD && current != null) {
            return current.rotation().y;
        }
        return entity.getViewYRot(partialTick);
    }

    private float currentPitch(float partialTick) {
        if (settings.offsetSpace() == EntityCameraSettings.OffsetSpace.WORLD && current != null) {
            return current.rotation().x;
        }
        return entity.getViewXRot(partialTick);
    }

    private @NotNull CameraState blendState(@NotNull CameraState from, @NotNull TargetState to) {
        CameraState target = to.state();
        return new CameraState(
                settings.positionInterpolation().interpolate(new PositionInterpolation.Context(from.position(), target.position(), to.anchor())),
                settings.rotationMode().interpolate(new RotationMode.InterpolationContext(from.rotation(), target.rotation()))
        );
    }

    private static float currentPartialTick(@NotNull Minecraft minecraft) {
        if (minecraft.level == null) {
            return 1.0F;
        }
        return minecraft.getTimer().getGameTimeDeltaPartialTick(minecraft.level.tickRateManager().runsNormally());
    }

    private record TargetState(@NotNull CameraState state, @NotNull Vec3 anchor) {
        private TargetState {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(anchor, "anchor");
        }
    }
}
