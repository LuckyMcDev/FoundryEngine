package io.github.luckymcdev.foundryengine.client.editor.popup;

import imgui.ImGui;
import imgui.flag.ImGuiPopupFlags;
import net.minecraft.resources.Identifier;

/**
 * A base class for creating and managing ImGui popups in the Foundry Engine.
 * <p>
 * This class provides functionality to create, open, close, and render both modal and non-modal popups.
 * It serves as a foundation for custom popups by allowing subclasses to override the {@link #content()} method.
 * </p>
 */
public class PopUp {
    /**
     * The unique identifier for this popup.
     */
    private final Identifier id;

    /**
     * The string identifier used by ImGui to manage the popup.
     */
    private final String strId;

    /**
     * Flags that control the behavior of the popup (e.g., no move, no resize).
     * Uses constants from {@link ImGuiPopupFlags}.
     */
    private final int flags;

    /**
     * Indicates whether this popup is modal.
     * <p>
     * Modal popups block interaction with other windows until they are closed.
     * </p>
     */
    private final boolean modal;

    /**
     * Constructs a non-modal popup with default flags.
     *
     * @param id    The unique identifier for this popup.
     * @param strId The string identifier used by ImGui to manage the popup.
     */
    public PopUp(Identifier id, String strId) {
        this(id, strId, false, ImGuiPopupFlags.None);
    }

    /**
     * Constructs a popup with customizable modal behavior and flags.
     *
     * @param id         The unique identifier for this popup.
     * @param strId      The string identifier used by ImGui to manage the popup.
     * @param modal      If {@code true}, the popup will be modal (blocks interaction with other windows).
     * @param popUpFlags Flags to control the popup's behavior. See {@link ImGuiPopupFlags} for available options.
     */
    public PopUp(Identifier id, String strId, boolean modal, int popUpFlags) {
        this.id = id;
        this.strId = strId;
        this.modal = modal;
        this.flags = popUpFlags;
    }

    /**
     * Returns the unique identifier of this popup.
     *
     * @return The popup's {@link Identifier}.
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Returns the string identifier used by ImGui to manage this popup.
     *
     * @return The ImGui string identifier.
     */
    public String getStrId() {
        return strId;
    }

    /**
     * Checks if this popup is currently open.
     *
     * @return {@code true} if the popup is open, {@code false} otherwise.
     */
    public boolean isOpen() {
        return ImGui.isPopupOpen(strId);
    }

    /**
     * Opens this popup.
     * <p>
     * This method triggers the popup to appear in the next ImGui frame.
     * </p>
     */
    public void open() {
        ImGui.openPopup(this.strId);
    }

    /**
     * Closes this popup if it is currently open.
     * <p>
     * This method ensures the popup is closed gracefully by checking its state first.
     * </p>
     */
    public void close() {
        if (ImGui.isPopupOpen(strId)) {
            ImGui.closeCurrentPopup();
        }
    }

    /**
     * Handles the rendering of this popup.
     * <p>
     * This method begins the popup context, renders its content, and ensures proper cleanup afterward.
     * The actual content is delegated to the {@link #content()} method, which can be overridden by subclasses.
     * </p>
     */
    public void handleRender() {
        boolean isOpen = modal ? ImGui.beginPopupModal(strId, flags) : ImGui.beginPopup(strId, flags);

        if (isOpen) {
            this.content();
            ImGui.endPopup();
        }
    }

    /**
     * Renders the content of this popup.
     * <p>
     * Subclasses should override this method to define custom content for the popup.
     * The default implementation does nothing.
     * </p>
     */
    public void content() {
    }
}
