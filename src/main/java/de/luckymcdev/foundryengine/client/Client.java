package de.luckymcdev.foundryengine.client;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.area.AreaRenderer;
import de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager;
import de.luckymcdev.foundryengine.client.editor.EditorController;
import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.MainMenu;
import de.luckymcdev.foundryengine.client.editor.feature.CutsceneEditorFeature;
import de.luckymcdev.foundryengine.client.imgui.ImGuiManager;
import de.luckymcdev.foundryengine.client.particle.ParticleManager;
import de.luckymcdev.foundryengine.client.post.PostEffectManager;
import de.luckymcdev.foundryengine.client.render.MeshRenderer;
import de.luckymcdev.foundryengine.client.render.obj.ObjModelManager;
import de.luckymcdev.foundryengine.client.skybox.SkyboxManager;
import de.luckymcdev.foundryengine.client.util.key.KeyBinding;
import de.luckymcdev.foundryengine.client.util.key.KeyBindingManager;
import de.luckymcdev.foundryengine.client.waypoint.WaypointRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.UtilityClassException;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.mutable.MutableFloat;
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

/**
 * Client-side singleton providing access to all client subsystems and utility methods.
 */
public final class Client {
    public static final Matrix4f MODEL_VIEW = new Matrix4f();
    public static final Matrix4f PROJECTION = new Matrix4f();
    public static final Matrix4f WORLD = new Matrix4f();
    public static final Matrix4f INVERSE_WORLD = new Matrix4f();
    public static final Matrix4f PERSPECTIVE = new Matrix4f();
    public static final Matrix4f FRUSTUM = new Matrix4f();
    public static final MutableFloat DEPTH_FAR = new MutableFloat(65536F);
    public static final MutableFloat DEPTH_NEAR = new MutableFloat(0.05F);

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
    public static final KeyBinding MENU_BAR_KEY = new KeyBinding(
            new KeyMapping(
                    Component.translatable("key.foundryengine.menu_bar").getString(),
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_F6,
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
    private static final WaypointRenderer WAYPOINT_RENDERER = new WaypointRenderer();
    private static final AreaRenderer AREA_RENDERER = new AreaRenderer();
    private static final MeshRenderer MESH_RENDERER = new MeshRenderer();
    private static final ObjModelManager OBJ_MODEL_MANAGER = new ObjModelManager();
    private static final ClientCutsceneManager CUTSCENE_MANAGER = new ClientCutsceneManager();
    private static final PostEffectManager POST_EFFECT_MANAGER = new PostEffectManager();
    private static final SkyboxManager SKYBOX_MANAGER = new SkyboxManager();
    private static final EditorController EDITOR_CONTROLLER = new EditorController();

    private Client() {
        throw new UtilityClassException();
    }

    /**
     * Returns the Minecraft instance via the engine interface.
     */
    public static Minecraft getMc() {
        return ((EngineMinecraft) Minecraft.getInstance()).engine$self();
    }

    /**
     * Returns the current resource manager.
     */
    public static ResourceManager getResourceManager() {
        return getMc().getResourceManager();
    }

    /**
     * Returns the main game window.
     */
    public static Window getWindow() {
        return getMc().getWindow();
    }

    /**
     * Returns the game renderer.
     */
    public static GameRenderer getGameRenderer() {
        return getMc().gameRenderer;
    }

    /**
     * Returns the main render target.
     */
    public static RenderTarget getMainRenderTarget() {
        return getMc().getMainRenderTarget();
    }

    /**
     * Returns the main game camera.
     */
    public static Camera getMainCamera() {
        return getGameRenderer().getMainCamera();
    }

    /**
     * Returns the client network connection, or null.
     */
    public static @Nullable ClientPacketListener getConnection() {
        return getMc().getConnection();
    }

    /**
     * Returns the local player, or null.
     */
    public static @Nullable LocalPlayer getPlayer() {
        return getMc().player;
    }

    /**
     * Sets the current screen overlay.
     */
    public static void setScreen(Screen screen) {
        getMc().setScreen(screen);
    }

    /**
     * Sends a chat command to the server.
     */
    public static void sendCommand(String command) {
        Objects.requireNonNull(getConnection()).sendCommand(command);
    }

    /**
     * Sends a network packet to the server.
     */
    public static void sendPacket(Packet<?> packet) {
        Objects.requireNonNull(getConnection()).send(packet);
    }

    /**
     * Returns the recipe manager from the integrated server, or null.
     */
    public static @Nullable RecipeManager getRecipeManager() {
        Minecraft mc = getMc();
        if (mc.getSingleplayerServer() != null) {
            return mc.getSingleplayerServer().getRecipeManager();
        }
        return null;
    }

    /**
     * Returns the ImGui manager.
     */
    public static ImGuiManager getImGuiManager() {
        return IMGUI_MANAGER;
    }

    /**
     * Returns the main menu.
     */
    public static MainMenu getMainMenu() {
        return MAIN_MENU;
    }

    /**
     * Returns the editor manager.
     */
    public static EditorManager getEditorManager() {
        return EDITOR_MANAGER;
    }

    /**
     * Returns the key binding manager.
     */
    public static KeyBindingManager getKeyBindingManager() {
        return KEY_BINDING_MANAGER;
    }

    /**
     * Returns the particle manager.
     */
    public static ParticleManager getParticleManager() {
        return PARTICLE_MANAGER;
    }

    /**
     * Returns the mesh renderer.
     */
    public static MeshRenderer getMeshRenderer() {
        return MESH_RENDERER;
    }

    /**
     * Returns the OBJ model manager.
     */
    public static ObjModelManager getObjModelManager() {
        return OBJ_MODEL_MANAGER;
    }

    /**
     * Returns the skybox manager.
     */
    public static SkyboxManager getSkyboxManager() {
        return SKYBOX_MANAGER;
    }

    /**
     * Returns the OpenGL device from the GPU backend.
     */
    public static GlDevice getGlDevice() {
        return (GlDevice) ((EngineGpuDevice) RenderSystem.getDevice()).engine$getBackend();
    }

    /**
     * Returns the color texture of the main render target.
     */
    public static GlTexture getGlColTexture() {
        return getGlColTexture(getMainRenderTarget());
    }

    /**
     * Returns the color texture of the given render target.
     */
    public static GlTexture getGlColTexture(RenderTarget target) {
        return unwrapTexture(target.getColorTexture());
    }

    /**
     * Returns the depth texture of the main render target.
     */
    public static GlTexture getGlDepthTexture() {
        return getGlDepthTexture(getMainRenderTarget());
    }

    /**
     * Returns the depth texture of the given render target.
     */
    public static GlTexture getGlDepthTexture(RenderTarget target) {
        return unwrapTexture(target.getDepthTexture());
    }

    /**
     * Unwraps a Minecraft texture object to its GL texture handle.
     */
    public static GlTexture unwrapTexture(Object tex) {
        return (GlTexture) tex;
    }

    /**
     * Returns the waypoint renderer.
     */
    public static WaypointRenderer getWaypointRenderer() {
        return WAYPOINT_RENDERER;
    }

    /**
     * Returns the area renderer.
     */
    public static AreaRenderer getAreaRenderer() {
        return AREA_RENDERER;
    }

    /**
     * Returns the client cutscene manager.
     */
    public static ClientCutsceneManager getCutsceneManager() {
        return CUTSCENE_MANAGER;
    }

    /**
     * Returns the post-effect manager.
     */
    public static PostEffectManager getPostEffectManager() {
        return POST_EFFECT_MANAGER;
    }

    /**
     * Returns the editor controller.
     */
    public static EditorController getEditorController() {
        return EDITOR_CONTROLLER;
    }

    /**
     * Returns the cutscene editor feature.
     */
    public static CutsceneEditorFeature getCutsceneEditor() {
        return EDITOR_CONTROLLER.getCutsceneEditorFeature();
    }

    /**
     * Returns the block position the player is looking at, or null.
     */
    public static @Nullable Vec3i getBlockHitOrNull() {
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

    /**
     * Updates the main view and projection matrices and their derived matrices.
     */
    public static void updateMain(Matrix4fc modelView, Matrix4fc projection) {
        MODEL_VIEW.set(modelView);
        PROJECTION.set(projection);
        WORLD.set(projection).mul(modelView);
        INVERSE_WORLD.set(WORLD).invert();
    }
}