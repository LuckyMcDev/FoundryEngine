package de.luckymcdev.foundryengine.mixin.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin {

    /*

    This is currently borked for some reason.

    /**
     * @author LuckyMcDev
     * @reason To change the distance Block Entities are rendered.
    @Overwrite
    default int getViewDistance() {
        return ClientConfig.COMPUTED_BLOCK_ENTITY_RENDER_DISTANCE;
    }
    */
}
