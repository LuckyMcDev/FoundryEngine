package de.luckymcdev.foundryengine.client.render.entity;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EngineItemDisplayRenderer
        extends DisplayRenderer.ItemDisplayRenderer {

    public EngineItemDisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}