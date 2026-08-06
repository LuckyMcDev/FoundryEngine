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
import net.minecraft.client.StringSplitter;
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
public final class ImGuiManager implements ResourceManagerReloadListener, NativeResource {
	public static final ImGuiCharSink IMGUI_CHAR_SINK = new ImGuiCharSink();
	public static final StringSplitter IM_GUI_SPLITTER = new StringSplitter((charId, style) -> {
		ImGui.pushFont(ImGraphicsExtractor.getStyleFont(style), 0.0F);
		float width = ImGui.calcTextSizeX(Character.toString(charId));
		ImGui.popFont();
		return width;
	});
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ImGuiImplGlfw imGuiImplGlfw = new ImGuiImplGlfw();
	private final ImGuiImplGl3 imGuiImplGl3 = new ImGuiImplGl3();
	private final ImGuiFontManager fontManager = new ImGuiFontManager(imGuiImplGl3);
	private final AtomicBoolean enabled = new AtomicBoolean(false);
	private final AtomicBoolean menuBarVisible = new AtomicBoolean(true);
	private @Nullable ImGuiContextStack imGuiContextStack;
	private boolean shouldBlockInput = false;
	private int previousFramebuffer;
	private int dockId;
	private ImTheme currentTheme;

	/**
	 * Creates a new ImGui context for the given window handle.
	 * See {@link GameRendererMixin#engine$renderHead(DeltaTracker, boolean, CallbackInfo)} for usage.
	 *
	 * @param handle          the window handle, e.g. {@link Window#handle()}
	 * @param resourceManager the resource manager for loading fonts
	 */
	public void create(final long handle, final ResourceManager resourceManager) {
		// Initialize ImGui, ImPlot, and ImNodes contexts
		final ImGuiContext imGuiContext = ImGui.createContext();
		final ImPlotContext imPlotContext = ImPlot.createContext();
		final ImNodesContext imNodesContext = ImNodes.createContext();
		imGuiContextStack = new ImGuiContextStack(imGuiContext, imPlotContext, imNodesContext);
		imGuiContextStack.push();

		final ImGuiIO io = ImGui.getIO();
		io.setIniFilename("feimgui.ini");
		io.setLogFilename("feimguilog.log");
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
		io.getFonts().setFreeTypeRenderer(true);
		imGuiImplGl3.init("#version 330 core");
		imGuiImplGlfw.init(handle, true);

		io.setConfigDockingWithShift(true);
		io.setConfigWindowsMoveFromTitleBarOnly(true); // Sadly, this breaks when using gizmos
		io.setConfigMacOSXBehaviors(InputQuirks.ON_OSX);

		fontManager.load(resourceManager, BuiltInFonts.ALL);

		ImGui.styleColorsDark();
		loadThemeFromConfig();
	}

	/**
	 * Enables ImGui rendering and input processing.
	 */
	public void enable() {
		enabled.set(true);
	}

	/**
	 * Disables ImGui rendering and input processing.
	 */
	public void disable() {
		enabled.set(false);
	}

	/**
	 * Shows the menu bar.
	 */
	public void showMenuBar() {
		menuBarVisible.set(true);
	}

	/**
	 * Hides the menu bar.
	 */
	public void hideMenuBar() {
		menuBarVisible.set(false);
	}

	/**
	 * Toggles the menu bar visibility.
	 */
	public void toggleMenuBar() {
		menuBarVisible.set(!menuBarVisible.get());
	}

	/**
	 * Returns whether the menu bar is currently visible.
	 */
	public boolean isMenuBarVisible() {
		return menuBarVisible.get();
	}

	/**
	 * Toggles the enabled state.
	 */
	public void toggle() {
		if (isEnabled()) {
			disable();
		} else {
			enable();
		}
	}

	/**
	 * Returns whether ImGui rendering is currently enabled.
	 */
	public boolean isEnabled() {
		return enabled.get();
	}

	private ImTheme loadThemeFromConfig() {
		ImTheme theme = ClientConfig.SELECTED_THEME.get().getTheme();
		setTheme(theme);
		return theme;
	}

	/**
	 * Persists the given theme name to the client config.
	 */
	public void saveThemeToConfig(ImTheme theme) {
		ClientConfig.SELECTED_THEME.set(ImThemes.get(theme));
		ClientConfig.SELECTED_THEME.save();
	}

	/**
	 * Applies the given theme and saves it to config.
	 */
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

	/**
	 * Returns the currently active theme.
	 */
	public ImTheme getCurrentTheme() {
		return currentTheme;
	}

	/**
	 * Prepares the frame for ImGui rendering: custom framebuffer, ImGui new frame, docking setup.
	 */
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

		previousFramebuffer = GL11.glGetInteger(GL30C.GL_FRAMEBUFFER_BINDING);
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


		dockId = ImGui.dockSpaceOverViewport(ImGui.getID(ImGui.getMainViewport().ptr), ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode + ImGuiDockNodeFlags.AutoHideTabBar);
		ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);
		shouldBlockInput = centralNode.isLeafNode() && !centralNode.isEmpty();
	}

	/**
	 * Ends ImGui rendering, draws the result and restores the default framebuffer.
	 */
	public void end() {
		if (!enabled.get()) {
			return;
		}

		try {
			ImGui.render();
			imGuiImplGl3.renderDrawData(ImGui.getDrawData());
		} finally {
			GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
		}

		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			final long pointer = GLFW.glfwGetCurrentContext();
			ImGui.updatePlatformWindows();
			ImGui.renderPlatformWindowsDefault();
			GLFW.glfwMakeContextCurrent(pointer);
		}
	}

	/**
	 * Returns the current dock space ID.
	 */
	public int getDockId() {
		return dockId;
	}

	/**
	 * Returns the font manager used for custom font configuration.
	 */
	public ImGuiFontManager getFontManager() {
		return fontManager;
	}

	/**
	 * Returns whether ImGui should capture mouse input.
	 */
	public boolean shouldInterceptMouse() {
		if (!enabled.get()) {
			return false;
		}
		return shouldBlockInput || (ImGui.getIO().getWantCaptureMouse() && !Client.getMc().mouseHandler.isMouseGrabbed());
	}

	public boolean shouldInterceptKeyboard() {
		if (!enabled.get()) {
			return false;
		}
		return shouldBlockInput || ImGui.getIO().getWantCaptureKeyboard();
	}

	/**
	 * Disposes all ImGui implementations and resources.
	 * Called from {@link MinecraftMixin#engine$close(CallbackInfo)} and {@link #free()}.
	 */
	public void dispose() {
		fontManager.destroy();
		if (Client.getImGraphics().getStackDepth() > 0) {
			Client.getImGraphics().popStack();
		}
		imGuiImplGl3.shutdown();
		imGuiImplGlfw.shutdown();
		imGuiContextStack.destroy();
	}

	/**
	 * Disposes all ImGui resources.
	 */
	@Override
	public void free() {
		dispose();
	}

	/**
	 * Reloads fonts from the resource manager on resource reload.
	 */
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		switch (ClientConfig.FONT_OPTION.get()) {
			case MINIMAL -> fontManager.load(resourceManager, BuiltInFonts.MINIMAL, BuiltInFonts.FALLBACK);
			case NORMAL -> fontManager.load(resourceManager, BuiltInFonts.ALL, BuiltInFonts.REGULAR);
		}
	}
}
