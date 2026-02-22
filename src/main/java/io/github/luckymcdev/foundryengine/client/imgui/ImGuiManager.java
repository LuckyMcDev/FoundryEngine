/*
 * This file is part of fabric-imgui-example-mod - https://github.com/FlorianMichael/fabric-imgui-example-mod
 * by FlorianMichael/EnZaXD and contributors
 */
package io.github.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import imgui.*;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.glfw.ImGuiImplGlfw;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.imgui.backend.FeImGuiImplGlfw;
import io.github.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import io.github.luckymcdev.foundryengine.client.imgui.context.ImGuiContextStack;
import io.github.luckymcdev.foundryengine.client.imgui.context.ImGuiContextTypes;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphics;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import io.github.luckymcdev.foundryengine.common.font.TTFFile;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Central ImGui Manager.
 * Manages The low level ImGui Hooks and also has {@link ImGuiImplGlfw} and {@link ImGuiImplGl3} contexts.
 * It uses OpenGl version 4.1 as the version for {@link ImGuiImplGl3#init(String version)}
 */
public final class ImGuiManager implements EngineImGui, ResourceManagerReloadListener, NativeResource {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final FeImGuiImplGlfw imGuiImplGlfw = new FeImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private final ImGuiContextStack CONTEXT_STACK = new ImGuiContextStack();
    private final ImGuiGraphicsStack GRAPHICS_STACK = new ImGuiGraphicsStack();

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private ImFont font;

    /**
     * The Glyph Ranges for the {@link TTFFile#JETBRAINS_MONO_NERDFONT_REGULAR} Font.
     */
    private final short[] GLYPH_RANGES = {
            0x0020, 0x00FF, // Basic Latin
            0x0100, 0x017F, // Latin Extended-A
            0x0400, 0x052F, // Cyrillic
            0x3040, 0x30FF, // Hiragana & Katakana
            (short) 0x4E00, (short) 0x9FFF, // CJK Unified Ideographs (Kanji, BMP portion)
            (short) 0xE0A0, (short) 0xE0A2, // Powerline symbols
            (short) 0xE000, (short) 0xE00A, // Pomicons
            (short) 0xE200, (short) 0xE2A9, // FA Extension
            (short) 0xE5FA, (short) 0xE6B7, // Seti-UI
            (short) 0xE700, (short) 0xE8EF, // Devicons
            (short) 0xED00, (short) 0xF2FF, // Font Awesome
            (short) 0xE300, (short) 0xE3E3, // Weather Icons
            (short) 0xF400, (short) 0xF533, // Octicons
            0x2665, 0x26A1, // Extra Octicons
            0
    };

    int dockId;
    boolean infoBarEnabled = false;

    public ImGuiManager() {
    }
    /**
     * Creates a new ImGui context for the Window handle
     * See Implementation {@link io.github.luckymcdev.foundryengine.mixin.render.GameRendererMixin#tb$renderHead(DeltaTracker, boolean, CallbackInfo)}
     *
     * @param handle the Window handle to use. Eg: {@link com.mojang.blaze3d.platform.Window#handle()}
     */
    @Override
    public void create(final long handle) {

        CONTEXT_STACK.addContextType(ImGuiContextTypes.IMGUI);
        CONTEXT_STACK.addContextType(ImGuiContextTypes.IMPLOT);
        CONTEXT_STACK.addContextType(ImGuiContextTypes.IMNODES);

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("toolbox.ini");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
        io.setConfigDockingWithShift(false);
        io.setConfigWindowsMoveFromTitleBarOnly(true);
        io.setConfigMacOSXBehaviors(InputQuirks.ON_OSX);

        imGuiImplGl3.init("#version 410 core");
        imGuiImplGlfw.init(handle, true);

        var style = ImGui.getStyle();
        ImGui.styleColorsDark();
        ImGuiGraphics.setFullDefaultStyle(style);
    }

    @Override
    public void enable() {
        enabled.set(true);
    }

    @Override
    public void disable() {
        enabled.set(false);
    }

    /**
     * Toggles the ImGui state between enabled and disabled.
     */
    @Override
    public void toggle() {
        if (isEnabled()) {
            disable();
        } else {
            enable();
        }
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Begins Rendering. Sets up custom FrameBuffer and other handling for ImGui rendering.
     */
    @Override
    public void begin() {
        if (!enabled.get()) return;

        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        GlTexture colorTexture = Client.getGlColTexture();
        GlDevice device = Client.getGlDevice();

        GlStateManager._glBindFramebuffer(
                GL30C.GL_FRAMEBUFFER, colorTexture.getFbo(device.directStateAccess(), null)
        );
        GL11.glViewport(0, 0, framebuffer.width, framebuffer.height);

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();

        final ImGuiIO io = ImGui.getIO();
        Minecraft mc = Client.getMinecraft();

        if (mc.mouseHandler.isMouseGrabbed()) {
            io.setMousePos(-1, -1);
        }

        dockId = ImGui.dockSpaceOverViewport(2087402907, ImGui.getMainViewport(), ImGuiDockNodeFlags.NoDockingInCentralNode | ImGuiDockNodeFlags.PassthruCentralNode);
        var centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);
        var centralNodePos = centralNode.getPos();
        var centralNodeSize = centralNode.getSize();

        int menuBarHeight = 20;

        int flags = ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.MenuBar
                | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoDocking
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.NoDecoration;


        if (!infoBarEnabled) return;

        ImGui.setNextWindowViewport(ImGui.getMainViewport().getID());
        ImGui.setNextWindowPos(centralNodePos.x, centralNodePos.y);
        ImGui.setNextWindowSize(centralNodeSize.x, menuBarHeight);

        if (ImGui.begin("###top-info-bar", flags)) {
            ImGui.setWindowFontScale(0.9F);
            if (ImGui.beginMenuBar()) {
                sideInfoBar();
                ImGui.endMenuBar();
            }
        }

        ImGui.end();
    }

    /**
     * Ends ImGui Rendering, drawing via {@link ImGuiImplGl3#renderDrawData(ImDrawData)} with {@link ImDrawData} being
     * accessed by {@link ImGui#getDrawData()}
     */
    @Override
    public void end() {
        if (!enabled.get()) return;
        ImGui.render();
        imGuiImplGl3.renderDrawData(ImGui.getDrawData());

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long pointer = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();

            GLFW.glfwMakeContextCurrent(pointer);
        }
    }

    /**
     * Returns the main {@link ImGuiContextStack}
     * @return the {@link ImGuiContextStack}
     */
    public ImGuiContextStack getMainContextStack() {
        return CONTEXT_STACK;
    }

    /**
     * Returns the Main {@link ImGuiGraphicsStack} although you should be able to create a new one using
     * {@link ImGuiGraphicsStack} constructor.
     * @return the {@link ImGuiGraphicsStack}
     */
    public ImGuiGraphicsStack getGraphicsStack() {
        return GRAPHICS_STACK;
    }

    /**
     * Loads the {@link TTFFile#JETBRAINS_MONO_NERDFONT_REGULAR} font with a resource Manager.
     * @param resourceManager the {@link ResourceManager} with which to access the resources.
     * Handles a null Font / an error during Font Loading and goes back to {@link ImFontAtlas#addFontDefault()}
     */
    public void loadFonts(ResourceManager resourceManager) {
        var fonts = ImGui.getIO().getFonts();
        fonts.clear();

        var config = new ImFontConfig();
        config.setGlyphRanges(GLYPH_RANGES);
        config.setOversampleH(3);
        config.setOversampleV(3);
        config.setRasterizerMultiply(1.2f);
        config.setGlyphOffset(0, 0);

        try {
            var bytes = TTFFile.JETBRAINS_MONO_NERDFONT_REGULAR.load(resourceManager);
            font = fonts.addFontFromMemoryTTF(bytes, 20F, config);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            fonts.addFontDefault();
        }
        imGuiImplGl3.destroyFontsTexture();
        fonts.build();
        imGuiImplGl3.createFontsTexture();
        config.destroy();
        fonts.clearTexData();


        if (ImGui.getFont() == null) {
            ImGui.getIO().getFonts().addFontDefault();
            LOGGER.error("Go back to default font, font corrupted?");
        }
    }

    /**
     * Weather ImGui should intercept Mouse movement.
     * @return if ImGui wants to capture the Mouse and the Mouse is not grabbed by Minecraft.
     */
    public boolean shouldInterceptMouse() {
        return ImGui.getIO().getWantCaptureMouse() && !Client.getMinecraft().mouseHandler.isMouseGrabbed();
    }

    /**
     * Weather ImGui should intercept Mouse movement
     * @return if ImGui wants to capture keyboard.
     */
    public boolean shouldInterceptKeyboard() {
        return ImGui.getIO().getWantCaptureKeyboard();
    }

    public ImFont getFont() {
        return font;
    }

    /**
     * Renders a Side Info Bar, IS NOT ENABLED!
     */
    public void sideInfoBar() {
        var now = new Date();

        String username = Client.getMinecraft().getUser().getName();
        ImGui.text(ImIcons.FA.FA_USER + " " + username);
        ImGui.separator();

        ImGui.text(ImIcons.FA.FA_EARTH_EUROPE + " " + now);
        ImGui.separator();

        ImGui.text(ImIcons.FA.FA_TACHOMETER + " " + Client.getMinecraft().getFps() + " FPS");
        ImGui.separator();

        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        ImGui.text(ImIcons.FA.FA_MEMORY + " Used " + (usedMemory * 100 / maxMemory) + "% Memory");
        ImGui.separator();

        var server = Client.getMinecraft().getCurrentServer();
        if (server != null) {
            ImGui.text(ImIcons.FA.FA_SERVER + " " + server.ip);
        }
    }

    /**
     * Disposes of all Implementations and the 2 Stacks.
     * Called in {@link io.github.luckymcdev.foundryengine.mixin.MinecraftMixin#tb$close(CallbackInfo)}
     * amd {@link ImGuiManager#free()} which is from {@link NativeResource}
     */
    public void dispose() {
        imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        CONTEXT_STACK.destroy();
        GRAPHICS_STACK.destroy();
    }

    @Override
    public void free() {
        dispose();
    }

    /**
     * Reloads the Font if the Client Resources are Reloaded.
     * @param resourceManager passed by {@link ResourceManagerReloadListener}
     */
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        loadFonts(resourceManager);
    }
}