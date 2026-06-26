package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.Client;
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

/**
 * Overrides camera position and rotation during cutscenes.
 */
@Mixin(Camera.class)
public abstract class CutsceneCameraMixin {
    @Shadow
    private Vec3 position;

    @Shadow
    @Final
    private BlockPos.MutableBlockPos blockPosition;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    /**
     * Injects before isDetached to detach camera when player is far from cutscene camera.
     */
    @Inject(method = "isDetached", at = @At("HEAD"), cancellable = true)
    private void engine$detachCameraDuringCutscene(CallbackInfoReturnable<Boolean> cir) {
        if (Client.getCutsceneManager().isCameraOverrideDisabled()) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 cam = Client.getCutsceneManager().getPos();
        if (!player.getEyePosition().closerThan(cam, ClientCutsceneManager.RENDER_PLAYER_RANGE)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Injects at tail of setPosition to override camera with cutscene manager values.
     */
    @Inject(method = "setPosition(Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
    private void engine$overridePosition(Vec3 ignored, CallbackInfo ci) {
        if (Client.getCutsceneManager().isCameraOverrideDisabled()) return;

        Vec3 newPos = Client.getCutsceneManager().getPos();
        var newRot = Client.getCutsceneManager().getRot();

        this.position = newPos;
        this.blockPosition.set(newPos.x, newPos.y, newPos.z);
        this.setRotation(newRot.y, newRot.x);
    }
}
