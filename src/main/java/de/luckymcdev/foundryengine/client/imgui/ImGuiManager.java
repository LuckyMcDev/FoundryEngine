/*
 * This file is part of fabric-imgui-example-mod - https://github.com/FlorianMichael/fabric-imgui-example-mod
 * by FlorianMichael/EnZaXD and contributors
 */
package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGlfw;
import de.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import de.luckymcdev.foundryengine.common.font.TTFFile;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.mixin.MinecraftMixin;
import de.luckymcdev.foundryengine.mixin.render.GameRendererMixin;
import imgui.*;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.internal.ImGuiContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Central ImGui Manager.
 * Manages The low level ImGui Hooks and also has {@link ImGuiImplGlfw} and {@link ImGuiImplGl3} contexts.
 * It uses OpenGl version 4.1 as the version for {@link ImGuiImplGl3#init(String version)}
 */
public final class ImGuiManager implements EngineImGui, ResourceManagerReloadListener, NativeResource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();
    private final ImGuiGraphicsStack graphicsStack = new ImGuiGraphicsStack();
    private final AtomicBoolean enabled = new AtomicBoolean(false);
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
    private @Nullable ImGuiContext imGuiContext;
    private @Nullable ImPlotContext imPlotContext;
    private @Nullable ImNodesContext imNodesContext;
    private boolean shouldBlockInput = false;
    private @Nullable ImFont font;
    private int dockId;
    private ImTheme currentTheme;

    public ImGuiManager() {
        //font = ImGui.getFont();
    }

    /**
     * Creates a new ImGui context for the Window handle
     * See Implementation {@link GameRendererMixin#engine$renderHead(DeltaTracker, boolean, CallbackInfo)}
     *
     * @param handle the Window handle to use. Eg: {@link com.mojang.blaze3d.platform.Window#handle()}
     */
    @Override
    public void create(final long handle) {
        imGuiContext = ImGui.createContext();
        imPlotContext = ImPlot.createContext();
        imNodesContext = ImNodes.createContext();
        ImGui.setCurrentContext(imGuiContext);
        ImPlot.setCurrentContext(imPlotContext);
        ImNodes.setCurrentContext(imNodesContext);


        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename("foundryengine.ini");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.getFonts().setFreeTypeRenderer(true);
        io.setConfigDockingWithShift(true);
        io.setConfigWindowsMoveFromTitleBarOnly(true);
        io.setConfigMacOSXBehaviors(InputQuirks.ON_OSX);

        imGuiImplGl3.init("#version 410 core");
        imGuiImplGlfw.init(handle, true);

        var fonts = io.getFonts();
        if (!fonts.isBuilt()) fonts.build();

        ImGui.styleColorsDark();

        loadThemeFromConfig();
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

    private void loadThemeFromConfig() {
        String themeName = ClientConfig.SELECTED_THEME.get();
        ImTheme theme = ImThemes.getThemeByName(themeName);
        setTheme(theme);
    }

    public void saveThemeToConfig(ImTheme theme) {
        ClientConfig.SELECTED_THEME.set(theme.getName());
        ClientConfig.SELECTED_THEME.save();
    }

    public void setTheme(ImTheme theme) {
        setTheme(theme, true);
    }

    public void setTheme(ImTheme theme, boolean saveToConfig) {
        theme.apply(ImGui.getStyle());
        this.currentTheme = theme;
        if (saveToConfig) {
            saveThemeToConfig(theme);
        }
        LOGGER.info("Applied theme '{}'", theme.getName());
    }

    public ImTheme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Begins Rendering. Sets up custom FrameBuffer and other handling for ImGui rendering.
     */
    @Override
    public void begin() {
        final ImGuiIO io = ImGui.getIO();

        if (!enabled.get()) {
            io.setWantCaptureKeyboard(false);
            io.setWantCaptureMouse(false);
            return;
        }

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

        Minecraft mc = Client.getMinecraft();

        if (mc.mouseHandler.isMouseGrabbed()) {
            io.setMousePos(-1, -1);
        }

        dockId = ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);
        imgui.internal.ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);

        shouldBlockInput = centralNode.isLeafNode() && !centralNode.isEmpty();
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
     * Returns the Main {@link ImGuiGraphicsStack} although you should be able to create a new one using
     * {@link ImGuiGraphicsStack} constructor.
     *
     * @return the {@link ImGuiGraphicsStack}
     */
    public ImGuiGraphicsStack getGraphicsStack() {
        return graphicsStack;
    }

    public int getDockId() {
        return dockId;
    }

    /**
     * Loads the {@link TTFFile#JETBRAINS_MONO_NERDFONT_REGULAR} font with a resource Manager.
     *
     * @param resourceManager the {@link ResourceManager} with which to access the resources.
     *                        Handles a null Font / an error during Font Loading and goes back to {@link ImFontAtlas#addFontDefault()}
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

        boolean fontLoadSuccess = false;
        try {
            var bytes = TTFFile.JETBRAINS_MONO_NERDFONT_REGULAR.load(resourceManager);
            font = fonts.addFontFromMemoryTTF(bytes, 20F, config);
            fontLoadSuccess = (font != null);
        } catch (Exception e) {
            LOGGER.error("Failed to load custom font: {}", e.getMessage(), e);
        }

        if (!fontLoadSuccess) {
            LOGGER.warn("Using default font due to custom font load failure");
            font = fonts.addFontDefault();
        }

        if (!fonts.build()) {
            LOGGER.error("Failed to build font atlas!");
            fonts.clear();
            font = fonts.addFontDefault();
            fonts.build();
        }

        imGuiImplGl3.destroyFontsTexture();
        imGuiImplGl3.createFontsTexture();

        config.destroy();
        fonts.clearTexData();

        if (ImGui.getFont() == null) {
            LOGGER.error("Font still null after loading, reinitializing with default");
            fonts.clear();
            fonts.addFontDefault();
            fonts.build();
            imGuiImplGl3.destroyFontsTexture();
            imGuiImplGl3.createFontsTexture();
        }
    }

    /**
     * Weather ImGui should intercept Mouse movement.
     *
     * @return if ImGui wants to capture the Mouse and the Mouse is not grabbed by Minecraft.
     */
    public boolean shouldInterceptMouse() {
        return shouldBlockInput || (ImGui.getIO().getWantCaptureMouse() && !Client.getMinecraft().mouseHandler.isMouseGrabbed());
    }

    /**
     * Weather ImGui should intercept Mouse movement
     *
     * @return if ImGui wants to capture keyboard.
     */
    public boolean shouldInterceptKeyboard() {
        return shouldBlockInput || ImGui.getIO().getWantCaptureKeyboard();
    }

    public ImFont getFont() {
        return font;
    }

    /**
     * Disposes of all Implementations and the 2 Stacks.
     * Called in {@link MinecraftMixin#engine$close(CallbackInfo)}
     * amd {@link ImGuiManager#free()} which is from {@link NativeResource}
     */
    public void dispose() {
        imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        ImGui.destroyContext(imGuiContext);
        ImPlot.destroyContext(imPlotContext);
        ImNodes.destroyContext(imNodesContext);
        graphicsStack.destroy();
    }

    @Override
    public void free() {
        dispose();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (Util.getPlatform() == Util.OS.WINDOWS) {
            loadFonts(resourceManager);
        } else {
            LOGGER.info("Hey, You're not on Windows, which means you'll sadly see a lot of ? in the editor, as the icons im using dont really support anything else.");
        }
    }
}