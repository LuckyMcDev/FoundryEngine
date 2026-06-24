package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor for PostPass custom uniform buffers.
 */
@Mixin(PostPass.class)
public interface PostEffectPassAccessor {

    @Accessor("customUniforms")
    Map<String, GpuBuffer> engine$getUniformBuffers();
}
