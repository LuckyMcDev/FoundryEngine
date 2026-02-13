package io.github.luckymcdev.common;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;

public interface Instances {
    static Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    static ResourceManager getResourceManager() {
        return getMinecraft().getResourceManager();
    }

    static Window getWindow() {
        return getMinecraft().getWindow();
    }

    static GameRenderer getGameRenderer() {
        return getMinecraft().gameRenderer;
    }

    static RenderTarget getMainRenderTarget() {
        return getMinecraft().getMainRenderTarget();
    }

    static Camera getMainCamera() {
        return getGameRenderer().getMainCamera();
    }

    static GlDevice getGlDevice() {
        ValidationGpuDevice mcDevice = (ValidationGpuDevice) RenderSystem.getDevice();
        return (GlDevice) mcDevice.getRealDevice();
    }

    static GlTexture getGlColTexture(RenderTarget target) {
        ValidationGpuTexture mcColTex = (ValidationGpuTexture) target.getColorTexture();
        return (GlTexture) mcColTex.getRealTexture();
    }

    static GlTexture getGlColTexture() {
        RenderTarget framebuffer = Instances.getMinecraft().getMainRenderTarget();
        ValidationGpuTexture mcColTex = (ValidationGpuTexture) framebuffer.getColorTexture();
        return (GlTexture) mcColTex.getRealTexture();
    }

    static GlTexture getGlDepthTexture() {
        RenderTarget framebuffer = Instances.getMinecraft().getMainRenderTarget();
        ValidationGpuTexture mcDepthTex = (ValidationGpuTexture) framebuffer.getDepthTexture();
        return (GlTexture) mcDepthTex.getRealTexture();
    }

    static BuiltInEditor getBuiltInEditor() {
        return InstancesInternal.EDITOR;
    }
}
