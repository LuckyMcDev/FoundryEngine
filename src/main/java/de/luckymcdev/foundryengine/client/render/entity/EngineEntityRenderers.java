package de.luckymcdev.foundryengine.client.render.entity;

import de.luckymcdev.foundryengine.common.world.entity.EngineEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class EngineEntityRenderers {
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                EngineEntities.BLOCK_DISPLAY.get(),
                EngineBlockDisplayRenderer::new
        );
        event.registerEntityRenderer(
                EngineEntities.ITEM_DISPLAY.get(),
                EngineItemDisplayRenderer::new
        );
        event.registerEntityRenderer(
                EngineEntities.TEXT_DISPLAY.get(),
                EngineTextDisplayRenderer::new
        );
    }
}