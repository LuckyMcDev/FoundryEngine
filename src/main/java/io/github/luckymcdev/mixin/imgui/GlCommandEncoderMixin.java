package io.github.luckymcdev.mixin.imgui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import io.github.luckymcdev.client.imgui.ImGuiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {

    // Modify the constant 0s in blitFrameBuffers call for X coordinates
    @ModifyConstant(method = "presentTexture", constant = {
            @Constant(intValue = 0, ordinal = 0),
            @Constant(intValue = 0, ordinal = 2)
    })
    private int modifyX(int original) {
        return ImGuiImpl.frameX(original);
    }

    // Modify the constant 0s in blitFrameBuffers call for Y coordinates
    @ModifyConstant(method = "presentTexture", constant = {
            @Constant(intValue = 0, ordinal = 1),
            @Constant(intValue = 0, ordinal = 3)
    })
    private int modifyY(int original) {
        return ImGuiImpl.frameY(original);
    }

    // Modify the getWidth calls
    @ModifyExpressionValue(method = "presentTexture", at = {
            @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTextureView;getWidth(I)I", ordinal = 0),
            @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTextureView;getWidth(I)I", ordinal = 2)
    })
    private int modifyWidth(int original) {
        return ImGuiImpl.frameW(original);
    }

    // Modify the getHeight calls
    @ModifyExpressionValue(method = "presentTexture", at = {
            @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTextureView;getHeight(I)I", ordinal = 0),
            @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTextureView;getHeight(I)I", ordinal = 2)
    })
    private int modifyHeight(int original) {
        return ImGuiImpl.frameH(original);
    }
}