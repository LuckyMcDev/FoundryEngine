package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import de.luckymcdev.foundryengine.interfaces.EnginePostChain;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

/**
 * Implements {@link EnginePostChain} on PostChain for external target processing.
 */
@Mixin(PostChain.class)
public abstract class PostChainMixin implements EnginePostChain {

    @Shadow
    public abstract void addToFrame(FrameGraphBuilder frame, int screenWidth, int screenHeight, PostChain.TargetBundle providedTargets);

    /**
     * Processes the post chain with the given external render targets.
     */
    @Unique
    @Override
    public void engine$process(Map<Identifier, RenderTarget> externalTargets, GraphicsResourceAllocator resourceAllocator) {
        if (externalTargets.isEmpty()) return;
        FrameGraphBuilder frame = new FrameGraphBuilder();
        LevelTargetBundle bundle = new LevelTargetBundle();

        RenderTarget mainTarget = externalTargets.get(LevelTargetBundle.MAIN_TARGET_ID);
        if (mainTarget == null) return;

        for (Map.Entry<Identifier, RenderTarget> entry : externalTargets.entrySet()) {
            Identifier id = entry.getKey();
            RenderTarget target = entry.getValue();
            bundle.replace(id, frame.importExternal(id.toString(), target));
        }

        this.addToFrame(frame, mainTarget.width, mainTarget.height, bundle);
        frame.execute(resourceAllocator);
    }
}