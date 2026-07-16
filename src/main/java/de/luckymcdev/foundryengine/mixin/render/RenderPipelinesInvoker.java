package de.luckymcdev.foundryengine.mixin.render;


import com.mojang.blaze3d.pipeline.RenderPipeline;
import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Allows registering {@link RenderPipeline}.
 */
@Mixin(RenderPipelines.class)
public interface RenderPipelinesInvoker {
	/**
	 * Registers a custom render pipeline via the Invoker.
	 */
	@Invoker("register")
	static RenderPipeline register(RenderPipeline pipeline) {
		throw new NoMixinException(RenderPipelinesInvoker.class);
	}
}
