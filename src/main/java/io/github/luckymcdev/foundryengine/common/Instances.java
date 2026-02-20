package io.github.luckymcdev.foundryengine.common;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.util.KeyBindingManager;
import io.github.luckymcdev.foundryengine.common.thread.ThreadManager;
import io.github.luckymcdev.foundryengine.interfaces.TbMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import net.neoforged.neoforge.common.NeoForge;

import static io.github.luckymcdev.foundryengine.common.InstancesInternal.*;

/**
 * Global access point for Minecraft and FoundryEngine instances.
 */
public interface Instances {

    // Minecraft Core

    static Minecraft getMinecraft() {
        return ((TbMinecraft) Minecraft.getInstance()).tb$self();
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

    // Rendering

    static GlDevice getGlDevice() {
        return (RenderSystem.getDevice() instanceof ValidationGpuDevice val) ? (GlDevice) val.getRealDevice() : (GlDevice) RenderSystem.getDevice();
    }

    static GlTexture getGlColTexture() {
        return getGlColTexture(getMainRenderTarget());
    }

    static GlTexture getGlColTexture(RenderTarget target) {
        return unwrapTexture(target.getColorTexture());
    }

    static GlTexture getGlDepthTexture() {
        return getGlDepthTexture(getMainRenderTarget());
    }

    static GlTexture getGlDepthTexture(RenderTarget target) {
        return unwrapTexture(target.getDepthTexture());
    }

    private static GlTexture unwrapTexture(Object tex) {
        return (tex instanceof ValidationGpuTexture val) ? (GlTexture) val.getRealTexture() : (GlTexture) tex;
    }

    // Engine Managers.

    static ImGuiManager getImGuiManager() {
        return IMGUI_MANAGER;
    }

    static EditorManager getEditorManager() {
        return EDITOR_MANAGER;
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

    static ThreadManager getThreadManager() {
        return THREAD_MANAGER;
    }

    // Event Bus

    static <T extends Event> T post(T event) {
        return NeoForge.EVENT_BUS.post(event);
    }

    static <T extends Event> T post(EventPriority priority, T event) {
        return NeoForge.EVENT_BUS.post(priority, event);
    }
}
