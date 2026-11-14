package de.luckymcdev.foundryengine.editor.client.popup;

import imgui.ImGui;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.resources.Identifier;

/**
 * A base class for creating and managing ImGui popups in the Foundry Engine.
 * <p>
 * Supports modal and non-modal popups. Subclasses override {@link #content()} to draw the popup body.
 * </p>
 * <p>
 * {@link #open()} and {@link #close()} may be called from anywhere (button handlers, ticks, other panels).
 * The actual ImGui calls are deferred to {@link #handleRender()}, because ImGui requires
 * {@code openPopup}, {@code beginPopup} and {@code closeCurrentPopup} to run inside a frame, at the
 * same ID-stack level, and {@code closeCurrentPopup} only works between begin and end of the popup.
 * Call {@link #handleRender()} every frame, at the same place in the UI where the popup was declared.
 * </p>
 */
public class PopUp {
	/**
	 * The unique identifier for this popup.
	 */
	private final Identifier id;

	/**
	 * The string identifier used by ImGui to manage the popup. For modals this is also the title.
	 */
	private final String strId;

	/**
	 * Flags passed to {@code openPopup} (see {@link ImGuiPopupFlags}).
	 */
	private final int popupFlags;

	/**
	 * Flags passed to {@code beginPopup} / {@code beginPopupModal} (see {@link ImGuiWindowFlags}).
	 */
	private final int windowFlags;

	/**
	 * Modal popups block interaction with everything else until closed.
	 */
	private final boolean modal;

	private boolean openRequested;
	private boolean closeRequested;

	/**
	 * Constructs a non-modal popup with default flags.
	 *
	 * @param id    The unique identifier for this popup.
	 * @param strId The string identifier used by ImGui to manage the popup.
	 */
	public PopUp(Identifier id, String strId) {
		this(id, strId, false, ImGuiPopupFlags.None, ImGuiWindowFlags.None);
	}

	/**
	 * Constructs a popup with customizable modal behavior and popup flags.
	 *
	 * @param id         The unique identifier for this popup.
	 * @param strId      The string identifier used by ImGui to manage the popup.
	 * @param modal      If {@code true}, the popup is modal.
	 * @param popUpFlags {@link ImGuiPopupFlags} applied when the popup is opened.
	 */
	public PopUp(Identifier id, String strId, boolean modal, int popUpFlags) {
		this(id, strId, modal, popUpFlags, ImGuiWindowFlags.None);
	}

	/**
	 * Constructs a popup with customizable modal behavior, popup flags and window flags.
	 *
	 * @param id          The unique identifier for this popup.
	 * @param strId       The string identifier used by ImGui to manage the popup.
	 * @param modal       If {@code true}, the popup is modal.
	 * @param popUpFlags  {@link ImGuiPopupFlags} applied when the popup is opened.
	 * @param windowFlags {@link ImGuiWindowFlags} applied to the popup window (e.g. {@code AlwaysAutoResize}).
	 */
	public PopUp(Identifier id, String strId, boolean modal, int popUpFlags, int windowFlags) {
		this.id = id;
		this.strId = strId;
		this.modal = modal;
		this.popupFlags = popUpFlags;
		this.windowFlags = windowFlags;
	}

	public Identifier getId() {
		return id;
	}

	public String getStrId() {
		return strId;
	}

	/**
	 * @return {@code true} if the popup is open or about to be opened on the next render.
	 */
	public boolean isOpen() {
		return openRequested || ImGui.isPopupOpen(strId);
	}

	/**
	 * Requests this popup to open. It appears when {@link #handleRender()} next runs.
	 */
	public void open() {
		closeRequested = false;
		openRequested = true;
	}

	/**
	 * Requests this popup to close. It is closed when {@link #handleRender()} next runs
	 * (or right after {@link #content()} if called from within it).
	 */
	public void close() {
		openRequested = false;
		closeRequested = true;
	}

	/**
	 * Opens, draws and closes the popup as requested. Call every frame.
	 */
	public void handleRender() {
		if (openRequested) {
			openRequested = false;
			ImGui.openPopup(strId, popupFlags);
		}

		boolean visible = modal
			? ImGui.beginPopupModal(strId, windowFlags)
			: ImGui.beginPopup(strId, windowFlags);

		if (!visible) {
			// Not open (or dismissed by the user): drop stale close requests.
			closeRequested = false;
			return;
		}

		try {
			if (!closeRequested) {
				this.content();
			}
			if (closeRequested) {
				closeRequested = false;
				ImGui.closeCurrentPopup();
			}
		} finally {
			ImGui.endPopup();
		}
	}

	/**
	 * Renders the content of this popup. Subclasses override this; the default does nothing.
	 */
	public void content() {
	}
}