package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

import java.util.Map;

public interface EnginePostChain extends EngineInterface<PostChain> {
    void engine$process(Map<Identifier, RenderTarget> externalTargets, GraphicsResourceAllocator resourceAllocator);
}