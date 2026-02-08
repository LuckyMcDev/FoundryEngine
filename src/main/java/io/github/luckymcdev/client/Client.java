package io.github.luckymcdev.client;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.interfaces.TbWindow;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;

public interface Client {
    static GlDevice getGlDevice() {
        ValidationGpuDevice mcDevice = (ValidationGpuDevice) RenderSystem.getDevice();
        return (GlDevice) mcDevice.getRealDevice();
    }

    static GlTexture getGlTexture() {
        RenderTarget framebuffer = Instances.getMinecraft().getMainRenderTarget();
        ValidationGpuTexture mcColTex = (ValidationGpuTexture) framebuffer.getColorTexture();
        return (GlTexture) mcColTex.getRealTexture();
    }

    static TbWindow convertToTb(Window window) {
        return (TbWindow) (Object) window;
    }
}
