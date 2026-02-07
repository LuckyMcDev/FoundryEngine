/*
 * This file is part of fabric-imgui-example-mod - https://github.com/FlorianMichael/fabric-imgui-example-mod
 * by FlorianMichael/EnZaXD and contributors
 */
package io.github.luckymcdev.client.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDir;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.github.luckymcdev.client.imgui.context.ImGuiContextStack;
import io.github.luckymcdev.client.imgui.context.ImGuiContextTypes;
import io.github.luckymcdev.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.common.Instances;
import io.github.luckymcdev.common.font.TTFFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

public final class ImGuiImpl {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final static ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final static ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();

    private final static ImGuiContextStack CONTEXT_STACK = new ImGuiContextStack();
    private final static ImGuiGraphicsStack GRAPHICS_STACK = new ImGuiGraphicsStack();

    private static short[] glyphRanges;

    public static ImFont font = null;

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

        // If you want to have custom fonts, you can use the following code here
        //font = loadFont("/fonts/jetbrainsmononerdfontmono-regular.ttf", 16);
        // In ImGui windows, you can set the font like this:
        //ImGui.pushFont(defaultFont);
        //ImGui.popFont();

        imGuiImplGl3.init();
        imGuiImplGlfw.init(handle, true);
        loadFonts(Instances.getResourceManager());
    }

    public static void beginImGuiRendering() {
        final RenderTarget framebuffer = Minecraft.getInstance().getMainRenderTarget();

        // This next "unwrapping" part is because of some weird neoforge shenanigans i dont understand.
        // There is basically some like validation thing, and you have to get the real thing??? im not really sure.

        // Unwrap Texture
        ValidationGpuTexture mcColTex = (ValidationGpuTexture) framebuffer.getColorTexture();
        GlTexture colorTexture = (GlTexture) mcColTex.getRealTexture();

        // Unwrap device
        ValidationGpuDevice mcDevice = (ValidationGpuDevice) RenderSystem.getDevice();
        GlDevice device = (GlDevice) mcDevice.getRealDevice();

        GlStateManager._glBindFramebuffer(
                GL30C.GL_FRAMEBUFFER, colorTexture.getFbo(device.directStateAccess(), null)
        );
        GL11.glViewport(0, 0, framebuffer.width, framebuffer.height);

        imGuiImplGl3.newFrame();
        imGuiImplGlfw.newFrame();
        ImGui.newFrame();
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

    /**
     * Get the main context stack for manual context switching
     */
    public static ImGuiContextStack getMainContextStack() {
        return CONTEXT_STACK;
    }

    /**
     * Get the main graphics stack
     */
    public static ImGuiGraphicsStack getGraphicsStack() {
        return GRAPHICS_STACK;
    }

    public static void loadFonts(ResourceManager resourceManager) {
        var fonts = ImGui.getIO().getFonts();
        fonts.clear();

        if (glyphRanges == null) {
            final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();

            rangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesDefault());
            rangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesCyrillic());
            rangesBuilder.addRanges(ImGui.getIO().getFonts().getGlyphRangesJapanese());

            glyphRanges = rangesBuilder.buildRanges();
        }

        var config = new ImFontConfig();
        config.setGlyphRanges(glyphRanges);
        config.setOversampleH(2);
        config.setOversampleV(2);
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