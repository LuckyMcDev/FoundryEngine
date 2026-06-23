package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGl3;
import de.luckymcdev.foundryengine.client.imgui.backend.ImGuiImplGlfw;
import de.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import de.luckymcdev.foundryengine.common.font.BuiltInFonts;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.mixin.MinecraftMixin;
import de.luckymcdev.foundryengine.mixin.render.GameRendererMixin;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.internal.ImGuiContext;
import imgui.internal.ImGuiDockNode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
 * Manages the low‑level ImGui hooks and holds {@link ImGuiImplGlfw} and {@link ImGuiImplGl3} contexts.
 * Font management is delegated to {@link ImGuiFontManager} for improved modularity.
 * Uses OpenGL version 330 core profile.
 */
public final class ImGuiManager implements EngineImGui, ResourceManagerReloadListener, NativeResource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();
    private final ImGuiGraphicsStack graphicsStack = new ImGuiGraphicsStack();
    private final ImGuiFontManager fontManager = new ImGuiFontManager(imGuiImplGl3);
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicBoolean menuBarVisible = new AtomicBoolean(true);
    private @Nullable ImGuiContext imGuiContext;
    private @Nullable ImPlotContext imPlotContext;
    private @Nullable ImNodesContext imNodesContext;
    private boolean shouldBlockInput = false;
    private int dockId;
    private ImTheme currentTheme;

    /**
     * Creates a new ImGui context for the given window handle.
     * See {@link GameRendererMixin#engine$renderHead(DeltaTracker, boolean, CallbackInfo)} for usage.
     *
     * @param handle the window handle, e.g. {@link Window#handle()}
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
        io.setIniFilename("feimgui.ini");
        io.setLogFilename("feimguilog.log");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
        io.getFonts().setFreeTypeRenderer(true);
        io.setConfigDockingWithShift(true);
        io.setConfigWindowsMoveFromTitleBarOnly(false);
        io.setConfigMacOSXBehaviors(InputQuirks.ON_OSX);

        BuiltInFonts.registerAll(fontManager);

        imGuiImplGl3.init("#version 330 core");
        imGuiImplGlfw.init(handle, true);

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

    @Override
    public void showMenuBar() {
        menuBarVisible.set(true);
    }

    @Override
    public void hideMenuBar() {
        menuBarVisible.set(false);
    }

    @Override
    public void toggleMenuBar() {
        menuBarVisible.set(!menuBarVisible.get());
    }

    @Override
    public boolean isMenuBarVisible() {
        return menuBarVisible.get();
    }

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

    private ImTheme loadThemeFromConfig() {
        String themeName = ClientConfig.SELECTED_THEME.get();
        ImTheme theme = ImThemes.getThemeByName(themeName);
        setTheme(theme);
        return theme;
    }

    public void saveThemeToConfig(ImTheme theme) {
        ClientConfig.SELECTED_THEME.set(theme.getName());
        ClientConfig.SELECTED_THEME.save();
    }

    @Override
    public void setTheme(ImTheme theme) {
        setTheme(theme, true);
    }

    @Override
    public void setTheme(ImTheme theme, boolean saveToConfig) {
        theme.apply(ImGui.getStyle());
        this.currentTheme = theme;
        if (saveToConfig) {
            saveThemeToConfig(theme);
        }
        LOGGER.info("Applied theme '{}'", theme.getName());
    }

    @Override
    public ImTheme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Prepares the frame for ImGui rendering: custom framebuffer, ImGui new frame, docking setup.
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

        if (Client.getMc().mouseHandler.isMouseGrabbed()) {
            io.setMousePos(-1, -1);
        }

        dockId = ImGui.dockSpaceOverViewport(ImGui.getMainViewport(),
                ImGuiDockNodeFlags.PassthruCentralNode + ImGuiDockNodeFlags.AutoHideTabBar);
        ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);
        shouldBlockInput = centralNode.isLeafNode() && !centralNode.isEmpty();
    }

    /**
     * Ends ImGui rendering, draws the result and restores the default framebuffer.
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

    @Override
    public ImGuiGraphicsStack getGraphicsStack() {
        return graphicsStack;
    }

    @Override
    public int getDockId() {
        return dockId;
    }

    /**
     * Returns the font manager used for custom font configuration.
     */
    @Override
    public ImGuiFontManager getFontManager() {
        return fontManager;
    }

    @Override
    public boolean shouldInterceptMouse() {
        return shouldBlockInput || (ImGui.getIO().getWantCaptureMouse() && !Client.getMc().mouseHandler.isMouseGrabbed());
    }

    @Override
    public boolean shouldInterceptKeyboard() {
        return shouldBlockInput || ImGui.getIO().getWantCaptureKeyboard();
    }

    /**
     * Disposes all ImGui implementations and resources.
     * Called from {@link MinecraftMixin#engine$close(CallbackInfo)} and {@link #free()}.
     */
    public void dispose() {
        fontManager.destroy();
        graphicsStack.destroy();
        imGuiImplGl3.shutdown();
        imGuiImplGlfw.shutdown();
        ImGui.destroyContext(imGuiContext);
        ImPlot.destroyContext(imPlotContext);
        ImNodes.destroyContext(imNodesContext);
    }

    @Override
    public void free() {
        dispose();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        LOGGER.info("Hey. Fonts might be broken. If they appear really bad for you, disable custom fonts in the config.");
        switch (ClientConfig.FONT_OPTION.get()) {
            case "MINIMAL": {
                fontManager.loadFonts(resourceManager, BuiltInFonts.MINIMAL_LIST);
                fontManager.setDefaultFont(BuiltInFonts.FALLBACK_JB);
                break;
            }
            case "NORMAL": {
                fontManager.loadFonts(resourceManager, BuiltInFonts.NORMAL_LIST);
                fontManager.setDefaultFont(BuiltInFonts.REGULAR);
                break;
            }
            case "DISABLED": {
                LOGGER.info("Fonts are Disabled.");
                break;
            }
        }
    }
}