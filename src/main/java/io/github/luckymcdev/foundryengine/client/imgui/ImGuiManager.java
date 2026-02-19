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
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.glfw.ImGuiImplGlfw;
import io.github.luckymcdev.foundryengine.client.imgui.backend.FeImGuiImplGlfw;
import io.github.luckymcdev.foundryengine.client.imgui.context.ImGuiContextStack;
import io.github.luckymcdev.foundryengine.client.imgui.context.ImGuiContextTypes;
import io.github.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphics;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.common.font.TTFFile;
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

import java.util.Date;

public final class ImGuiManager implements ResourceManagerReloadListener, NativeResource {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ImGuiImplGlfw imGuiImplGlfw = new FeImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private final ImGuiContextStack CONTEXT_STACK = new ImGuiContextStack();
    private final ImGuiGraphicsStack GRAPHICS_STACK = new ImGuiGraphicsStack();
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

        imGuiImplGl3.init();
        imGuiImplGlfw.init(handle, true);

        var style = ImGui.getStyle();
        ImGui.styleColorsDark();
        ImGuiGraphics.setFullDefaultStyle(style);
    }

    public void begin() {
        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        GlTexture colorTexture = Instances.getGlColTexture();
        GlDevice device = Instances.getGlDevice();

        GlStateManager._glBindFramebuffer(
                GL30C.GL_FRAMEBUFFER, colorTexture.getFbo(device.directStateAccess(), null)
        );
        GL11.glViewport(0, 0, framebuffer.width, framebuffer.height);

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();

        final ImGuiIO io = ImGui.getIO();
        Minecraft mc = Instances.getMinecraft();

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
                topInfoBar();
                ImGui.endMenuBar();
            }
        }

        ImGui.end();
    }

    public void end() {
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

    public ImGuiContextStack getMainContextStack() {
        return CONTEXT_STACK;
    }

    public ImGuiGraphicsStack getGraphicsStack() {
        return GRAPHICS_STACK;
    }

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
            fonts.addFontFromMemoryTTF(bytes, 20F, config);
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
            LOGGER.info("Go back to default font, font corrupted?");
        }
    }

    public boolean shouldInterceptMouse() {
        return ImGui.getIO().getWantCaptureMouse() && !Minecraft.getInstance().mouseHandler.isMouseGrabbed();
    }

    public boolean shouldInterceptKeyboard() {
        return ImGui.getIO().getWantCaptureKeyboard();
    }

    public void topInfoBar() {
        var now = new Date();

        String username = Minecraft.getInstance().getUser().getName();
        ImGui.text(ImIcons.FA.FA_USER + " " + username);
        ImGui.separator();

        ImGui.text(ImIcons.FA.FA_EARTH_EUROPE + " " + now);
        ImGui.separator();

        ImGui.text(ImIcons.FA.FA_TACHOMETER + " " + Minecraft.getInstance().getFps() + " FPS");
        ImGui.separator();

        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        ImGui.text(ImIcons.FA.FA_MEMORY + " Used " + (usedMemory * 100 / maxMemory) + "% Memory");
        ImGui.separator();

        var server = Minecraft.getInstance().getCurrentServer();
        if (server != null) {
            ImGui.text(ImIcons.FA.FA_SERVER + " " + server.ip);
        }
    }

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

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        loadFonts(resourceManager);
    }
}