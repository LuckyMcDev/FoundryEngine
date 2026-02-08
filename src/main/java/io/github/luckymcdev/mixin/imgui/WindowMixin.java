package io.github.luckymcdev.mixin.imgui;

import com.mojang.blaze3d.platform.Window;
import io.github.luckymcdev.interfaces.TbWindow;
import io.github.luckymcdev.client.imgui.ImGuiImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin implements TbWindow {
    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Shadow
    private int width;

    @Shadow
    private int height;

    @Shadow
    private int windowedWidth;

    @Shadow
    private int windowedHeight;

    @Unique
    private double tb$xScale = 1;

    @Unique
    private double tb$yScale = 1;

    @Unique
    private double tb$xOffset = 0;

    @Unique
    private double tb$yOffset = 0;

    @Unique
    private int tb$lastWidth = 1;
    @Unique
    private int tb$lastHeight = 1;

    @Unique
    private int tb$unscaledFramebufferWidth = 1;

    @Unique
    private int tb$unscaledFramebufferHeight = 1;

    @Unique
    private int tb$unscaledWidth = 1;

    @Unique
    private int tb$unscaledHeight = 1;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo ci) {
        ImGuiImpl.trackDpiScale((Window) (Object) this);
        tb$unscaledFramebufferWidth = framebufferWidth;
        tb$unscaledFramebufferHeight = framebufferHeight;
        tb$unscaledWidth = width;
        tb$unscaledHeight = height;
    }

    @ModifyVariable(method = "setWidth", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public int setFramebufferWidth(int width) {
        return tb$transformNewFramebufferWidth(width);
    }

    @ModifyVariable(method = "setHeight", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public int setFramebufferHeight(int height) {
        return tb$transformNewFramebufferHeight(height);
    }

    @Inject(method = "refreshFramebufferSize", at = @At("RETURN"))
    public void updateFramebufferSize(CallbackInfo ci) {
        framebufferWidth = tb$transformNewFramebufferWidth(framebufferWidth);
        framebufferHeight = tb$transformNewFramebufferHeight(framebufferHeight);
    }

    @ModifyVariable(method = "onFramebufferResize", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public int onFramebufferSizeChanged$width(int width) {
        return tb$transformNewFramebufferWidth(width);
    }

    @ModifyVariable(method = "onFramebufferResize", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
    public int onFramebufferSizeChanged$height(int height) {
        return tb$transformNewFramebufferHeight(height);
    }

    @ModifyVariable(method = "onResize", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public int onWindowSizeChanged$width(int width) {
        tb$unscaledWidth = width;
        return windowedWidth = (int) (tb$unscaledWidth * tb$xScale);
    }

    @ModifyVariable(method = "onResize", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
    public int onWindowSizeChanged$height(int height) {
        tb$unscaledHeight = height;
        return windowedHeight = (int) (tb$unscaledHeight * tb$yScale);
    }

    @Override
    public void tb$setViewportArea(double xOffset, double yOffset, double xScale, double yScale) {
        tb$xOffset = xOffset;
        tb$yOffset = yOffset;
        tb$xScale = xScale;
        tb$yScale = yScale;

        // Calculate new dimensions
        int newFramebufferWidth = (int) (tb$unscaledFramebufferWidth * xScale);
        int newFramebufferHeight = (int) (tb$unscaledFramebufferHeight * yScale);
        int newWidth = (int) (tb$unscaledWidth * xScale);
        int newHeight = (int) (tb$unscaledHeight * yScale);

        // Only update if dimensions actually changed
        if (newFramebufferWidth != tb$lastWidth || newFramebufferHeight != tb$lastHeight) {
            framebufferWidth = newFramebufferWidth;
            framebufferHeight = newFramebufferHeight;
            width = windowedWidth = newWidth;
            height = windowedHeight = newHeight;

            tb$lastWidth = newFramebufferWidth;
            tb$lastHeight = newFramebufferHeight;
        }
    }

    @Override
    public double tb$getXOffset() {
        return tb$xOffset;
    }

    @Override
    public double tb$getYOffset() {
        return tb$yOffset;
    }

    @Override
    public double tb$getInverseYOffset() {
        // EXACT vidlib formula
        return (1D - tb$yScale) - tb$yOffset;
    }

    @Override
    public int tb$getUnscaledWidth() {
        return tb$unscaledWidth;
    }

    @Override
    public int tb$getUnscaledHeight() {
        return tb$unscaledHeight;
    }

    @Override
    public int tb$getUnscaledFramebufferWidth() {
        return tb$unscaledFramebufferWidth;
    }

    @Override
    public int tb$getUnscaledFramebufferHeight() {
        return tb$unscaledFramebufferHeight;
    }

    @Unique
    private int tb$transformNewFramebufferWidth(int width) {
        tb$unscaledFramebufferWidth = width;
        return (int) (width * tb$xScale);
    }

    @Unique
    private int tb$transformNewFramebufferHeight(int height) {
        tb$unscaledFramebufferHeight = height;
        return (int) (height * tb$yScale);
    }
}