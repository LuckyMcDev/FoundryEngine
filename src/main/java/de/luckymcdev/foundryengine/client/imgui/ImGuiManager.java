package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.mixin.MinecraftMixin;
import de.luckymcdev.foundryengine.mixin.render.GameRendererMixin;
import foundry.imgui.api.ImGuiMC;
import foundry.imgui.impl.ImGuiMCImpl;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.internal.ImGuiDockNode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Central ImGui Manager.
 */
public final class ImGuiManager implements NativeResource {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final AtomicBoolean enabled = new AtomicBoolean(false);
	private final AtomicBoolean menuBarVisible = new AtomicBoolean(true);
	public static final Identifier FONT = Common.id("jetbrains_mono_nf");
	private @Nullable ImNodesContext imNodesContext;
	private boolean shouldBlockInput = false;
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
		imNodesContext = ImNodes.createContext();
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
		io.setConfigWindowsMoveFromTitleBarOnly(true); // Sadly, this breaks when using gizmos
		io.setConfigMacOSXBehaviors(InputQuirks.ON_OSX);
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

		if (Client.getMc().mouseHandler.isMouseGrabbed()) {
			io.setMousePos(-1, -1);
		}


		dockId = ImGui.dockSpaceOverViewport(ImGui.getID(ImGui.getMainViewport().ptr), ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode + ImGuiDockNodeFlags.AutoHideTabBar);
		ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);
		shouldBlockInput = centralNode.isLeafNode() && !centralNode.isEmpty();
	}

	/**
	 * Ends ImGui rendering, draws the result and restores the default framebuffer.
	 * This method is just fully useless.
	 */
	public void end() {
		// TODO: remove this method? Figure out what to do with it.
	}

	/**
	 * Returns the current dock space ID.
	 */
	public int getDockId() {
		return dockId;
	}

	/**
	 * Returns whether ImGui should capture mouse input.
	 */
	public boolean shouldInterceptMouse() {
		if (!enabled.get()) {
			return false;
		}
		if (shouldBlockInput) {
			return true;
		}
		try (ImGuiMC.ActiveContext ignored = ImGuiMC.withImGui()) {
			return ImGui.getIO().getWantCaptureMouse() && !Client.getMc().mouseHandler.isMouseGrabbed();
		}
	}

	public boolean shouldInterceptKeyboard() {
		if (!enabled.get()) {
			return false;
		}
		if (shouldBlockInput) {
			return true;
		}
		try (ImGuiMC.ActiveContext ignored = ImGuiMC.withImGui()) {
			return ImGui.getIO().getWantCaptureKeyboard();
		}
	}

	/**
	 * Disposes all ImGui implementations and resources.
	 * Called from {@link MinecraftMixin#engine$close(CallbackInfo)} and {@link #free()}.
	 */
	public void dispose() {
		if (Client.getImGraphics().getStackDepth() > 0) {
			Client.getImGraphics().popStack();
		}
		if (imNodesContext != null) {
			ImNodes.destroyContext(imNodesContext);
			imNodesContext = null;
		}
	}

	/**
	 * Disposes all ImGui resources.
	 */
	@Override
	public void free() {
		dispose();
	}
}
