package io.github.luckymcdev.common;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.TbRenderer;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.imgui.ImGuiHandler;
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

    static ImGuiHandler getImGuiHandler() {
        return InstancesInternal.IMGUI_HANDLER;
    }

    static GlTexture getGlColTexture(RenderTarget target) {
        var tex = target.getColorTexture();
        if (tex instanceof ValidationGpuTexture validationTex) {
            return (GlTexture) validationTex.getRealTexture();
        }
        return (GlTexture) tex;
    }

    static GlTexture getGlColTexture() {
        return getGlColTexture(getMinecraft().getMainRenderTarget());
    }

    static GlTexture getGlDepthTexture() {
        RenderTarget target = getMinecraft().getMainRenderTarget();
        var tex = target.getDepthTexture();
        if (tex instanceof ValidationGpuTexture validationTex) {
            return (GlTexture) validationTex.getRealTexture();
        }
        return (GlTexture) tex;
    }

    static GlDevice getGlDevice() {
        var device = RenderSystem.getDevice();
        if (device instanceof ValidationGpuDevice validationDevice) {
            return (GlDevice) validationDevice.getRealDevice();
        }
        return (GlDevice) device;
    }

    static BuiltInEditor getBuiltInEditor() {
        return InstancesInternal.EDITOR;
    }

    static TbRenderer getTbRenderer() {
        return InstancesInternal.RENDERER;
    }
}
