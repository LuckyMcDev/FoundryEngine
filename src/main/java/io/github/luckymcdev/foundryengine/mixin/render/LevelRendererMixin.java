package io.github.luckymcdev.foundryengine.mixin.render;

import io.github.luckymcdev.foundryengine.client.ClientMatrices;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to set {@link ClientMatrices#PERSPECTIVE} and {@link ClientMatrices#FRUSTUM}
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "prepareCullFrustum", at = @At("RETURN"))
    public void fe$prepareCullFrustum(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Vec3 cameraPosition, CallbackInfoReturnable<Frustum> cir) {
        ClientMatrices.PERSPECTIVE.set(projectionMatrix);
        ClientMatrices.FRUSTUM.set(frustumMatrix);
    }
}
