package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor to retrieve the native OpenGL handle from a GlBuffer.
 */
@Mixin(GlBuffer.class)
public interface GlGpuBufferAccessor {

    @Accessor("handle")
    int engine$getId();
}
