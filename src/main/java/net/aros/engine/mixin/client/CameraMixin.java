package net.aros.engine.mixin.client;

import net.aros.engine.client.camera.AeCameras;
import net.aros.engine.client.camera.CameraState;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch, float roll);

    @Shadow
    public abstract void setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick);

    @Unique
    private boolean aengine$settingUpVanillaCamera;

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void aengine$applyDynamicCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (aengine$settingUpVanillaCamera || !AeCameras.isActive()) {
            return;
        }

        aengine$settingUpVanillaCamera = true;
        try {
            setup(level, entity, detached, thirdPersonReverse, partialTick);
        } finally {
            aengine$settingUpVanillaCamera = false;
        }
        Camera self = (Camera) (Object) this;

        Optional<CameraState> maybeState = AeCameras.renderState(self, partialTick);
        if (maybeState.isEmpty()) {
            return;
        }

        this.detached = true;
        CameraState state = maybeState.get();
        this.setPosition(state.position());

        //noinspection UnstableApiUsage
        ViewportEvent.ComputeCameraAngles cameraAngles = NeoForge.EVENT_BUS.post(
                new ViewportEvent.ComputeCameraAngles(
                        self,
                        partialTick,
                        state.rotation().y,
                        state.rotation().x,
                        state.rotation().z
                )
        );
        this.setRotation(cameraAngles.getYaw(), cameraAngles.getPitch(), cameraAngles.getRoll());
        ci.cancel();
    }
}
