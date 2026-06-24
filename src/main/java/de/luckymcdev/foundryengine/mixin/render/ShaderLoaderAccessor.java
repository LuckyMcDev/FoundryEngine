package de.luckymcdev.foundryengine.mixin.render;

import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.ShaderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for ShaderManager's post-chain projection fields.
 */
@Mixin(ShaderManager.class)
public interface ShaderLoaderAccessor {

    @Accessor("postChainProjection")
    Projection engine$getProjection();

    @Accessor("postChainProjectionMatrixBuffer")
    ProjectionMatrixBuffer engine$getProjectionMatrixBuffer();
}
