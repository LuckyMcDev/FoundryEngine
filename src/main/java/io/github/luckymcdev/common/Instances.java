package io.github.luckymcdev.common;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.client.TbRenderer;
import io.github.luckymcdev.client.editor.BuiltInEditor;
import io.github.luckymcdev.client.imgui.ImGuiHandler;
import io.github.luckymcdev.client.opengl.OpenGlStack;
import io.github.luckymcdev.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.client.post.PostProcessManager;
import io.github.luckymcdev.client.util.KeyBindingManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;

import static io.github.luckymcdev.common.InstancesInternal.*;

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
        return getGlColTexture(getMainRenderTarget());
    }

    static GlTexture getGlDepthTexture(RenderTarget target) {
        var tex = target.getDepthTexture();
        if (tex instanceof ValidationGpuTexture validationTex) {
            return (GlTexture) validationTex.getRealTexture();
        }
        return (GlTexture) tex;
    }

    static GlTexture getGlDepthTexture() {
        return getGlDepthTexture(getMainRenderTarget());
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

    static OpenGlStack getOpenGlStack() {
        return OPEN_GL_STACK;
    }

    static PostProcessManager getPostProcessManager() {
        return POST_PROCESS_MANAGER;
    }
    static FrameBufferManager getFrameBufferManager() {
        return FRAME_BUFFER_MANAGER;
    }
    static ShaderManager getShaderManager() {
        return SHADER_MANAGER;
    }
    static KeyBindingManager getKeyBindingManager() {
        return KEY_BINDING_MANAGER;
    }
}
