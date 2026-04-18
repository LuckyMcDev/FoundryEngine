package de.luckymcdev.foundryengine.client.render.entity;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EngineBlockDisplayRenderer
        extends DisplayRenderer.BlockDisplayRenderer {

    public EngineBlockDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}