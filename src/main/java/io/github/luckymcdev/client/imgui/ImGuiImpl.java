/*
 * This file is part of fabric-imgui-example-mod - https://github.com/FlorianMichael/fabric-imgui-example-mod
 * by FlorianMichael/EnZaXD and contributors
 */
package io.github.luckymcdev.client.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.github.luckymcdev.client.Client;
import io.github.luckymcdev.client.imgui.context.ImGuiContextStack;
import io.github.luckymcdev.client.imgui.context.ImGuiContextTypes;
import io.github.luckymcdev.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.common.font.TTFFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.slf4j.Logger;

public final class ImGuiImpl {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final static ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private final static ImGuiContextStack CONTEXT_STACK = new ImGuiContextStack();
    private final static ImGuiGraphicsStack GRAPHICS_STACK = new ImGuiGraphicsStack();
    private static final short[] GLYPH_RANGES = {
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
    static int dockId;

    public static void create(final long handle) {

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

        loadFonts(Instances.getResourceManager());

        var style = ImGui.getStyle();
        ImGui.styleColorsDark();
        setFullDefaultStyle(style);
    }

    public static void beginImGuiRendering() {
        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();
        GlTexture colorTexture = Client.getGlTexture();
        GlDevice device = Client.getGlDevice();

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
    }

    public static void endImGuiRendering() {
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

    public static ImGuiContextStack getMainContextStack() {
        return CONTEXT_STACK;
    }

    public static ImGuiGraphicsStack getGraphicsStack() {
        return GRAPHICS_STACK;
    }

    public static void loadFonts(ResourceManager resourceManager) {
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
            LOGGER.error(e.getMessage());
            fonts.addFontDefault();
        }

        fonts.build();
        imGuiImplGl3.destroyFontsTexture();
        imGuiImplGl3.createFontsTexture();
        config.destroy();
        fonts.clearTexData();
    }

    public static boolean shouldInterceptMouse() {
        return ImGui.getIO().getWantCaptureMouse() && !Minecraft.getInstance().mouseHandler.isMouseGrabbed();
    }

    public static boolean shouldInterceptKeyboard() {
        return ImGui.getIO().getWantCaptureKeyboard();
    }

    public static void setFullDefaultStyle(ImGuiStyle style) {
        setDefaultStyle(style);
        style.setWindowPadding(4F, 4F);
        style.setFramePadding(4F, 1F);
        style.setPopupBorderSize(0F);
        style.setItemSpacing(6F, 4F);
        style.setItemInnerSpacing(8F, 6F);
    }

    public static void setDefaultStyle(ImGuiStyle style) {
        style.setWindowMenuButtonPosition(ImGuiDir.None);
        style.setWindowRounding(4F);
        style.setFrameRounding(3F);
        style.setChildRounding(3F);
        style.setPopupRounding(3F);
        style.setScrollbarRounding(1F);
        style.setGrabRounding(2F);
        style.setIndentSpacing(25F);
        style.setScrollbarSize(13F);
        style.setGrabMinSize(16F);
        style.setWindowBorderSize(0F);
        style.setSelectableTextAlign(0F, 0.5F);
        style.setAlpha(1F);

        setColor(style, ImGuiCol.WindowBg, 0xFF222228);
        setColor(style, ImGuiCol.PopupBg, 0xE30D0D11);
        setColor(style, ImGuiCol.FrameBg, 0xFF15151C);
        setColor(style, ImGuiCol.TitleBg, 0xFF010101);
        setColor(style, ImGuiCol.TitleBgActive, 0xFF010101);
        setColor(style, ImGuiCol.MenuBarBg, 0xFF17171C);
        setColor(style, ImGuiCol.TitleBgCollapsed, 0xEF517F70);
        setColor(style, ImGuiCol.SliderGrab, 0xFF446692);
        setColor(style, ImGuiCol.Button, 0x664296FA);
        setColor(style, ImGuiCol.ButtonHovered, 0x664296FA);
        setColor(style, ImGuiCol.ButtonActive, 0x664296FA);
    }

    public static void setColor(ImGuiStyle style, int key, int color) {
        style.setColor(key, ARGB.toABGR(color));
    }

    public static void dispose() {
        imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        CONTEXT_STACK.destroy();
        GRAPHICS_STACK.destroy();
    }
}