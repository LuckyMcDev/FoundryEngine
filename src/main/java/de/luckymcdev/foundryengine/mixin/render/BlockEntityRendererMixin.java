package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererMixin {

    /**
     * @author LuckyMcDev
     * @reason To change the distance Block Entities are rendered.
     */
    @Overwrite
    default int getViewDistance() {
        return ClientConfig.getComputedBlockEntityRenderDistance();
    }
}
