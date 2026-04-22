package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import de.luckymcdev.foundryengine.client.cutscene.ClientScreenEffectManager;
import de.luckymcdev.foundryengine.common.cutscene.util.ScreenEffectType;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PostPass.class)
public abstract class PostPassProgressMixin {

    @Final
    @Shadow
    private Map<String, GpuBuffer> customUniforms;

    @Final
    @Shadow
    private String name;

    @Unique
    private GpuBuffer progressBuffer;

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void engine$updateProgress(FrameGraphBuilder frame, Map<Identifier, ResourceHandle<RenderTarget>> targets, GpuBufferSlice shaderOrthoMatrix, CallbackInfo ci) {
        boolean isScreenEffect = false;
        for (ScreenEffectType type : ScreenEffectType.values()) {
            if (name != null && name.contains(type.name())) {
                isScreenEffect = true;
                break;
            }
        }
        if (!isScreenEffect) return;

        if (progressBuffer == null) {
            progressBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "CutsceneProgress",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                    16
            );
        }

        customUniforms.put("ProgressBuffer", progressBuffer);

        float progress = 0f;
        if (ClientScreenEffectManager.screenEffect != null) {
            progress = ClientScreenEffectManager.screenEffect.getProgress();
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(progressBuffer, false, true)) {
            Std140Builder std140 = Std140Builder.intoBuffer(view.data());
            std140.putFloat(progress);
        }
    }
}

