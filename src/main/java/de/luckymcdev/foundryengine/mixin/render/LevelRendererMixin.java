package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.Client;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to set {@link Client#PERSPECTIVE} and {@link Client#FRUSTUM}
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    /*
    TODO: fix this
    @Inject(method = "prepareCullFrustum", at = @At("RETURN"))
    public void fe$prepareCullFrustum(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Vec3 cameraPosition, CallbackInfoReturnable<Frustum> cir) {
        Client.PERSPECTIVE.set(projectionMatrix);
        Client.FRUSTUM.set(frustumMatrix);
    }
     */
}
