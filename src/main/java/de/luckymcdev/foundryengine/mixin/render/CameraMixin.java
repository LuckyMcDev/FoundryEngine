package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.Client;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private float depthFar;

    @Shadow
    private void setupPerspective(float zNear, float zFar, float fov, float width, float height) {
    }

    @Inject(
            method = "update",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Camera;depthFar:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER)
    )
    private void overrideDepthFar(DeltaTracker deltaTracker, CallbackInfo ci) {
        this.depthFar = Client.DEPTH_FAR.floatValue();
    }

    @Redirect(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V")
    )
    private void overrideSetupPerspective(Camera instance, float zNear, float zFar, float fov, float width, float height) {
        ((CameraMixin) (Object) instance).setupPerspective(Client.DEPTH_NEAR.floatValue(), Client.DEPTH_FAR.floatValue(), fov, width, height);
    }
}
