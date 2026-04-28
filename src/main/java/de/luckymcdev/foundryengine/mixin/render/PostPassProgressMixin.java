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
import org.jetbrains.annotations.Nullable;
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
    @Nullable
    private GpuBuffer engine$progressBuffer;

    @Unique
    private boolean engine$isScreenEffect;

    @Unique
    private boolean engine$checkedName;

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void engine$updateProgress(FrameGraphBuilder frame, Map<Identifier, ResourceHandle<RenderTarget>> targets, GpuBufferSlice shaderOrthoMatrix, CallbackInfo ci) {
        if (!engine$checkedName) {
            for (ScreenEffectType type : ScreenEffectType.values()) {
                if (name.contains(type.name())) {
                    engine$isScreenEffect = true;
                    break;
                }
            }
            engine$checkedName = true;
        }

        if (!engine$isScreenEffect) return;

        if (engine$progressBuffer == null) {
            engine$progressBuffer = RenderSystem.getDevice().createBuffer(
                    this::engine$getProgressBufferName,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                    16
            );
            customUniforms.put("ProgressBuffer", engine$progressBuffer);
        }

        float progress = ClientScreenEffectManager.screenEffect != null
                ? ClientScreenEffectManager.screenEffect.getProgress()
                : 0f;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(engine$progressBuffer, false, true)) {
            Std140Builder.intoBuffer(view.data()).putFloat(progress);
        }
    }

    @Unique
    private String engine$getProgressBufferName() {
        return "CutsceneProgress";
    }
}

