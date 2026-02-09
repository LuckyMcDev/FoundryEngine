package io.github.luckymcdev.common;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
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

    static GlDevice getGlDevice() {
        ValidationGpuDevice mcDevice = (ValidationGpuDevice) RenderSystem.getDevice();
        return (GlDevice) mcDevice.getRealDevice();
    }

    static GlTexture getGlTexture() {
        RenderTarget framebuffer = Instances.getMinecraft().getMainRenderTarget();
        ValidationGpuTexture mcColTex = (ValidationGpuTexture) framebuffer.getColorTexture();
        return (GlTexture) mcColTex.getRealTexture();
    }
}
