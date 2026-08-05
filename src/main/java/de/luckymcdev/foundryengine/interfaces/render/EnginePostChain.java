package de.luckymcdev.foundryengine.interfaces.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Processes a post-chain with external render targets and a graphics resource allocator.
 */
public interface EnginePostChain extends EngineInterface<PostChain> {
	/**
	 * Processes the post-chain using the given external targets and resource allocator.
	 */
	default void engine$process(Map<Identifier, RenderTarget> externalTargets, GraphicsResourceAllocator resourceAllocator) {
		throw new NoMixinException(this);
	}
}