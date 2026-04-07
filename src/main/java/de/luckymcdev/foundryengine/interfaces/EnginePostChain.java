package de.luckymcdev.foundryengine.interfaces;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.resources.Identifier;

import java.util.Map;

public interface EnginePostChain {
    void engine$process(Map<Identifier, RenderTarget> externalTargets, GraphicsResourceAllocator resourceAllocator);
}