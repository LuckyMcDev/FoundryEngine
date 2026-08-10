package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.config.ClientConfig;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.styles.ImTheme;
import de.luckymcdev.foundryengine.client.editor.styles.ImThemes;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.mixin.MinecraftMixin;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.internal.ImGuiDockNode;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The central ImGui manager. Configures the ImGui docking space, theme and editor enable state.
 * <p>
 * The ImGui context, renderer, fonts and viewports are all owned by ImGuiMC: see
 * the {@code ImGuiLoadEventsNeoforge} and {@code RenderImGuiEventsNeoforge} wiring in
 * {@code FoundryEngineModClient}.
 */
public final class ImGuiManager implements NativeResource {
	private final AtomicBoolean enabled = new AtomicBoolean(false);
	private final AtomicBoolean menuBarVisible = new AtomicBoolean(true);
	public static final Identifier FONT = Common.id("jetbrains_mono_nf");
	public static float scaleOverride = 1.4f;
	private @Nullable ImNodesContext imNodesContext;
	private boolean blockInput;
	private int dockId;
	private ImTheme currentTheme;

	/**
	 * Creates the ImNodes context and applies the ImGui IO flags.
	 * Called from {@code ImGuiLoadEventsNeoforge.Pre}, where the ImGui context is already current.
	 */
	public void create() {
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
	}

	/**
	 * Returns the currently active theme.
	 */
	public ImTheme getCurrentTheme() {
		return currentTheme;
	}

	/**
	 * Prepares the docking space for an ImGui frame.
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

		dockId = ImGui.dockSpaceOverViewport(
			ImGui.getID(ImGui.getMainViewport().ptr),
			ImGui.getMainViewport(),
			ImGuiDockNodeFlags.PassthruCentralNode + ImGuiDockNodeFlags.AutoHideTabBar
		);
		ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);
		blockInput = centralNode.isLeafNode() && !centralNode.isEmpty();
	}

	/**
	 * @return the current dock space ID.
	 */
	public int getDockId() {
		return dockId;
	}

	/**
	 * Returns whether a docked window fully covers the central dock node, in which case
	 * the game must not receive any input. This complements ImGuiMC's own want-capture
	 * flags, which are handled by ImGuiMC's mixins.
	 */
	public boolean shouldBlockInput() {
		return enabled.get() && blockInput;
	}

	/**
	 * Disposes all ImGui resources. Called from {@link MinecraftMixin#engine$close(CallbackInfo)}.
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