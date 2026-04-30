package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CutsceneCameraMixin {
    @Shadow
    private Vec3 position;

    @Shadow
    @Final
    private BlockPos.MutableBlockPos blockPosition;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "isDetached", at = @At("HEAD"), cancellable = true)
    private void engine$detachCameraDuringCutscene(CallbackInfoReturnable<Boolean> cir) {
        if (ClientCutsceneManager.isCameraOverrideDisabled()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 cam = ClientCutsceneManager.pos;
        if (!player.getEyePosition().closerThan(cam, ClientCutsceneManager.RENDER_PLAYER_RANGE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void engine$overridePosition(Vec3 ignored, CallbackInfo ci) {
        if (ClientCutsceneManager.isCameraOverrideDisabled()) return;

        Vec3 newPos = ClientCutsceneManager.pos;
        var newRot = ClientCutsceneManager.rot;

        this.position = newPos;
        this.blockPosition.set(newPos.x, newPos.y, newPos.z);
        this.setRotation(newRot.y, newRot.x);
    }
}
