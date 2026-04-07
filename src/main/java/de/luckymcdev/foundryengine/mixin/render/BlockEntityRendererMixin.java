package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
        int rd = Minecraft.getInstance().options.renderDistance().get() * 16;
        String val = ClientConfig.BLOCK_ENTITY_RENDER_DISTANCE.get();

        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        return switch (val) {
            case "full" -> rd;
            case "half" -> rd / 2;
            default -> 64;
        };
    }
}
