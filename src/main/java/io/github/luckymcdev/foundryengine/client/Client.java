package io.github.luckymcdev.foundryengine.client;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlStack;
import io.github.luckymcdev.foundryengine.client.opengl.framebuffer.FrameBufferManager;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.ShaderManager;
import io.github.luckymcdev.foundryengine.client.post.PostProcessManager;
import io.github.luckymcdev.foundryengine.client.util.KeyBinding;
import io.github.luckymcdev.foundryengine.client.util.KeyBindingManager;
import io.github.luckymcdev.foundryengine.interfaces.TbMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class Client {
    /**
     * The Model View Matrix
     */
    public static final Matrix4f MODEL_VIEW = new Matrix4f();
    /**
     * The Projection Matrix
     */
    public static final Matrix4f PROJECTION = new Matrix4f();
    /**
     * The World Matrix
     */
    public static final Matrix4f WORLD = new Matrix4f();
    /**
     * The Inverse World Matrix
     */
    public static final Matrix4f INVERSE_WORLD = new Matrix4f();
    /**
     * The Perspective Matrix
     */
    public static final Matrix4f PERSPECTIVE = new Matrix4f();
    /**
     * The Frustum Matrix
     */
    public static final Matrix4f FRUSTUM = new Matrix4f();
    public static final KeyBinding EDITOR_KEY = new KeyBinding(
            new KeyMapping(
                    Component.translatable("key.foundryengine.editor").getString(),
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F7,
                    KeyMapping.Category.DEBUG
            ),
            () -> {
            }
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    // Core Systems
    static OpenGlStack OPEN_GL_STACK = new OpenGlStack();
    static ShaderManager SHADER_MANAGER = new ShaderManager();
    static FrameBufferManager FRAME_BUFFER_MANAGER = new FrameBufferManager();
    static PostProcessManager POST_PROCESS_MANAGER = new PostProcessManager();
    static ImGuiManager IMGUI_MANAGER = new ImGuiManager();
    static EditorManager EDITOR_MANAGER = new EditorManager();
    static KeyBindingManager KEY_BINDING_MANAGER = new KeyBindingManager();

    // Minecraft Core

    public static Minecraft getMinecraft() {
        return ((TbMinecraft) Minecraft.getInstance()).tb$self();
    }

    public static ResourceManager getResourceManager() {
        return getMinecraft().getResourceManager();
    }

    public static Window getWindow() {
        return getMinecraft().getWindow();
    }

    public static GameRenderer getGameRenderer() {
        return getMinecraft().gameRenderer;
    }

    public static RenderTarget getMainRenderTarget() {
        return getMinecraft().getMainRenderTarget();
    }

    public static Camera getMainCamera() {
        return getGameRenderer().getMainCamera();
    }

    public static ClientPacketListener getConnection() {
        return getMinecraft().getConnection();
    }

    public static Player getPlayer() {
        return getMinecraft().player;
    }

    public static void setScreen(Screen screen) {
        getMinecraft().setScreen(screen);
    }

    public static void sendCommand(String command) {
        Objects.requireNonNull(getConnection()).sendCommand(command);
    }

    public static void sendPacket(Packet<?> packet) {
        Objects.requireNonNull(getConnection()).send(packet);
    }

    // Rendering

    public static GlDevice getGlDevice() {
        return (RenderSystem.getDevice() instanceof ValidationGpuDevice val) ? (GlDevice) val.getRealDevice() : (GlDevice) RenderSystem.getDevice();
    }

    public static GlTexture getGlColTexture() {
        return getGlColTexture(getMainRenderTarget());
    }

    public static GlTexture getGlColTexture(RenderTarget target) {
        return unwrapTexture(target.getColorTexture());
    }

    public static GlTexture getGlDepthTexture() {
        return getGlDepthTexture(getMainRenderTarget());
    }

    public static GlTexture getGlDepthTexture(RenderTarget target) {
        return unwrapTexture(target.getDepthTexture());
    }

    public static GlTexture unwrapTexture(Object tex) {
        return (tex instanceof ValidationGpuTexture val) ? (GlTexture) val.getRealTexture() : (GlTexture) tex;
    }

    // Engine Managers.

    public static ImGuiManager getImGuiManager() {
        return IMGUI_MANAGER;
    }

    public static EditorManager getEditorManager() {
        return EDITOR_MANAGER;
    }

    public static OpenGlStack getOpenGlStack() {
        return OPEN_GL_STACK;
    }

    public static PostProcessManager getPostProcessManager() {
        return POST_PROCESS_MANAGER;
    }

    public static FrameBufferManager getFrameBufferManager() {
        return FRAME_BUFFER_MANAGER;
    }

    public static ShaderManager getShaderManager() {
        return SHADER_MANAGER;
    }

    public static KeyBindingManager getKeyBindingManager() {
        return KEY_BINDING_MANAGER;
    }

    /**
     * Returns the Content of a {@link Identifier} pointer as a String.
     *
     * @param location the Location where to get the Contents.
     * @return the String content.
     */
    public static String getIdSource(Identifier location) {
        return getIdSource(location, StandardCharsets.UTF_8);
    }

    /**
     * {@link #getIdSource(Identifier)}
     * but with a specifiable {@link Charset}
     *
     * @param location the Identifier.
     * @param charset  the {@link Charset} with which to load the Identifier
     * @return the String Content of the File.
     */
    public static String getIdSource(Identifier location, Charset charset) {
        try (InputStream stream = Client.getResourceManager().getResourceOrThrow(location).open()) {
            // Read entire stream into byte array, then decode once
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, charset);
        } catch (IOException e) {
            LOGGER.error("Failed to load resource: {}", location, e);
            return "";
        }
    }

    /**
     * Alternative implementation with reusable buffer for very large files.
     */
    public static String getIdSourceBuffered(Identifier location, Charset charset) {
        try (InputStream stream = Client.getResourceManager().getResourceOrThrow(location).open();
             Reader reader = new InputStreamReader(stream, charset)) {

            StringBuilder sb = new StringBuilder(2048); // Pre-allocate reasonable size for shaders
            char[] buffer = new char[2048]; // Reusable buffer
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }

            return sb.toString();
        } catch (IOException e) {
            LOGGER.error("Failed to load resource: {}", location, e);
            return "";
        }
    }

    /**
     * Updates the {@link #MODEL_VIEW}, {@link #PROJECTION}, {@link #WORLD} and {@link #INVERSE_WORLD} Matrices.
     * <br>
     * Called from {@link FrameGraphSetupEvent}
     *
     * @param modelView  updated Model View Matrix.
     * @param projection updated Projection Matrix.
     */
    public static void updateMain(Matrix4fc modelView, Matrix4fc projection) {
        MODEL_VIEW.set(modelView);
        PROJECTION.set(projection);
        WORLD.set(projection).mul(modelView);
        INVERSE_WORLD.set(WORLD).invert();
    }
}
