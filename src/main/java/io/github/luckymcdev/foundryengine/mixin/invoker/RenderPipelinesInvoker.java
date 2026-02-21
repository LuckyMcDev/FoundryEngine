package io.github.luckymcdev.foundryengine.mixin.invoker;


import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.luckymcdev.foundryengine.common.exeptions.NoMixinException;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Allows registering {@link RenderPipeline}.
 */
@Mixin(RenderPipelines.class)
public interface RenderPipelinesInvoker {
    @Invoker("register")
    static RenderPipeline register(RenderPipeline pipeline) {
        throw new NoMixinException(RenderPipelinesInvoker.class);
    }
}
