package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
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
    void engine$process(Map<Identifier, RenderTarget> externalTargets, GraphicsResourceAllocator resourceAllocator);
}