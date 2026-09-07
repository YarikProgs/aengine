package net.aros.engine.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.aros.engine.client.camera.AeCameras;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void aengine$skipHurtBobForDynamicCamera(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        if (AeCameras.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void aengine$skipViewBobForDynamicCamera(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        if (AeCameras.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void aengine$skipHandForDynamicCamera(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (AeCameras.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void aengine$skipBlockOutlineForDynamicCamera(CallbackInfoReturnable<Boolean> cir) {
        if (AeCameras.isActive()) {
            cir.setReturnValue(false);
        }
    }
}
