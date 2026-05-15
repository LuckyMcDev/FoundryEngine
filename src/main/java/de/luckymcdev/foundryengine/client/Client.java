package de.luckymcdev.foundryengine.client;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.MainMenu;
import de.luckymcdev.foundryengine.client.imgui.EngineImGui;
import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.client.particle.ParticleManager;
import de.luckymcdev.foundryengine.client.post.EffectManager;
import de.luckymcdev.foundryengine.client.render.MeshRenderer;
import de.luckymcdev.foundryengine.client.render.obj.ObjModelManager;
import de.luckymcdev.foundryengine.client.util.key.KeyBinding;
import de.luckymcdev.foundryengine.client.util.key.KeyBindingManager;
import de.luckymcdev.foundryengine.client.waypoint.WaypointRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.interfaces.EngineGpuDevice;
import de.luckymcdev.foundryengine.interfaces.EngineMinecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class Client {
    public static final Matrix4f MODEL_VIEW = new Matrix4f();
    public static final Matrix4f PROJECTION = new Matrix4f();
    public static final Matrix4f WORLD = new Matrix4f();
    public static final Matrix4f INVERSE_WORLD = new Matrix4f();
    public static final Matrix4f PERSPECTIVE = new Matrix4f();
    public static final Matrix4f FRUSTUM = new Matrix4f();

    public static final KeyMapping.Category EDITOR_CATEGORY = new KeyMapping.Category(Common.id("editor"));
    public static final KeyBinding EDITOR_KEY = new KeyBinding(
            new KeyMapping(
                    Component.translatable("key.foundryengine.editor").getString(),
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F7,
                    EDITOR_CATEGORY
            ),
            () -> {
            }
    );
    public static final KeyMapping PRIMARY_WAYPOINT_KEY = new KeyMapping(
            "key.foundryengine.primary_waypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            EDITOR_CATEGORY
    );

    public static final KeyMapping SECONDARY_WAYPOINT_KEY = new KeyMapping(
            "key.foundryengine.secondary_waypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            EDITOR_CATEGORY
    );

    public static final KeyMapping REMOVE_WAYPOINT_KEY = new KeyMapping(
            "key.foundryengine.remove_waypoint",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            EDITOR_CATEGORY
    );
    public static final KeyMapping CLEAR_WAYPOINTS_KEY = new KeyMapping(
            "key.foundryengine.clear_waypoints",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            EDITOR_CATEGORY
    );
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ImGuiManager IMGUI_MANAGER = new ImGuiManager();
    private static final MainMenu MAIN_MENU = new MainMenu();
    private static final EditorManager EDITOR_MANAGER = new EditorManager();
    private static final KeyBindingManager KEY_BINDING_MANAGER = new KeyBindingManager();
    private static final ParticleManager PARTICLE_MANAGER = new ParticleManager();
    private static final EffectManager EFFECT_MANAGER = new EffectManager();
    private static final WaypointRenderer WAYPOINT_RENDERER = new WaypointRenderer();
    private static final MeshRenderer MESH_RENDERER = new MeshRenderer();
    private static final ObjModelManager OBJ_MODEL_MANAGER = new ObjModelManager();

    private Client() {
        throw new EngineException();
    }

    public static Minecraft getMc() {
        return ((EngineMinecraft) Minecraft.getInstance()).engine$self();
    }

    public static ResourceManager getResourceManager() {
        return getMc().getResourceManager();
    }

    public static Window getWindow() {
        return getMc().getWindow();
    }

    public static GameRenderer getGameRenderer() {
        return getMc().gameRenderer;
    }

    public static RenderTarget getMainRenderTarget() {
        return getMc().getMainRenderTarget();
    }

    public static Camera getMainCamera() {
        return getGameRenderer().getMainCamera();
    }

    public static @Nullable ClientPacketListener getConnection() {
        return getMc().getConnection();
    }

    public static @Nullable LocalPlayer getPlayer() {
        return getMc().player;
    }

    public static void setScreen(Screen screen) {
        getMc().setScreen(screen);
    }

    public static void sendCommand(String command) {
        Objects.requireNonNull(getConnection()).sendCommand(command);
    }

    public static void sendPacket(Packet<?> packet) {
        Objects.requireNonNull(getConnection()).send(packet);
    }

    public static EngineImGui getImGuiManager() {
        return IMGUI_MANAGER;
    }

    public static MainMenu getMainMenu() {
        return MAIN_MENU;
    }

    public static EditorManager getEditorManager() {
        return EDITOR_MANAGER;
    }

    public static KeyBindingManager getKeyBindingManager() {
        return KEY_BINDING_MANAGER;
    }

    public static ParticleManager getParticleManager() {
        return PARTICLE_MANAGER;
    }

    public static EffectManager getEffectManager() {
        return EFFECT_MANAGER;
    }

    public static MeshRenderer getMeshRenderer() {
        return MESH_RENDERER;
    }

    public static ObjModelManager getObjModelManager() {
        return OBJ_MODEL_MANAGER;
    }

    public static GlDevice getGlDevice() {
        return (GlDevice) ((EngineGpuDevice) RenderSystem.getDevice()).engine$getBackend();
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
        return (GlTexture) tex;
    }

    public static WaypointRenderer getWaypointRenderer() {
        return WAYPOINT_RENDERER;
    }

    public static @Nullable Vec3i getHitOrNull() {
        HitResult hit = getMc().hitResult;
        if (hit != null && hit.getType() == (HitResult.Type.BLOCK)) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            return blockHit.getBlockPos();

        }
        return null;
    }

    /**
     * Returns the content of the resource at {@code location} as a UTF-8 string.
     */
    public static String getIdSource(Identifier location) {
        return getIdSource(location, StandardCharsets.UTF_8);
    }

    /**
     * Returns the content of the resource at {@code location} decoded with {@code charset}.
     */
    public static String getIdSource(Identifier location, Charset charset) {
        try (InputStream stream = getResourceManager().getResourceOrThrow(location).open()) {
            return new String(stream.readAllBytes(), charset);
        } catch (IOException e) {
            LOGGER.error("Failed to load resource: {}", location, e);
            return "";
        }
    }

    public static void updateMain(Matrix4fc modelView, Matrix4fc projection) {
        MODEL_VIEW.set(modelView);
        PROJECTION.set(projection);
        WORLD.set(projection).mul(modelView);
        INVERSE_WORLD.set(WORLD).invert();
    }
}