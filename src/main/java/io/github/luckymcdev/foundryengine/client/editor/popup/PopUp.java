package io.github.luckymcdev.foundryengine.client.editor.popup;

import imgui.ImGui;
import imgui.flag.ImGuiPopupFlags;
import net.minecraft.resources.Identifier;

public class PopUp {
    private final Identifier id;
    private final String strId;
    private final int flags;
    private final boolean modal;

    public PopUp(Identifier id, String strId) {
        this(id, strId, false, ImGuiPopupFlags.None);
    }

    public PopUp(Identifier id, String strId, boolean modal, int popUpFlags) {
        this.id = id;
        this.strId = strId;
        this.modal = modal;
        this.flags = popUpFlags;
    }

    public Identifier getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public boolean isOpen() {
        return ImGui.isPopupOpen(strId);
    }

    public void open() {
        ImGui.openPopup(this.strId);
    }

    public void close() {
        if (ImGui.isPopupOpen(strId)) {
            ImGui.closeCurrentPopup();
        }
    }

    public void handleRender() {
        boolean isOpen = modal ? ImGui.beginPopupModal(strId, flags) : ImGui.beginPopup(strId, flags);

        if (isOpen) {
            this.content();
            ImGui.endPopup();
        }
    }

    public void content() {
    }
}
