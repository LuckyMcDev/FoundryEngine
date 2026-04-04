package de.luckymcdev.foundryengine.client.editor.builtin.tools;

import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiDragDropFlags;
import imgui.type.ImString;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;

public class CataloguePanel extends EditorPanel {
    public static final CataloguePanel INSTANCE = new CataloguePanel();
    private final ImString searchBuffer = new ImString(256);
    private final float itemSize = 64f;

    public CataloguePanel() {
        super(Common.id("catalogue"), "Catalogue", ImIcons.FA.FA_LIST, Shortcut.empty());
        this.category = PanelCategory.EDITOR_TOOLS;
    }

    /**
     * Helper to accept a drop from the Catalogue.
     *
     */
    public static void acceptDrop(Consumer<CataloguePayload> callback) {
        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("CATALOGUE_ENTRY");
            if (payload instanceof CataloguePayload data) {
                callback.accept(data);
            }
            ImGui.endDragDropTarget();
        }
    }

    @Override
    public void content() {
        renderSearchHeader();

        if (ImGui.beginTabBar("CatalogueTabs")) {
            if (ImGui.beginTabItem(ImIcons.FA.FA_BOX + " Items")) {
                renderRegistryGrid("items", BuiltInRegistries.ITEM.keySet().stream().toList());
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem(ImIcons.FA.FA_CUBE + " Blocks")) {
                renderRegistryGrid("blocks", BuiltInRegistries.BLOCK.keySet().stream().toList());
                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }
    }

    private void renderSearchHeader() {
        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("##catalogue_search", ImIcons.FA.FA_MAGNIFYING_GLASS + " Search registries...", searchBuffer);
        ImGui.separator();
    }

    private void renderRegistryGrid(String id, List<Identifier> entries) {
        String filter = searchBuffer.get().toLowerCase();

        ImGui.beginChild("##grid_" + id, 0, 0, false);

        float windowVisibleX2 = ImGui.getWindowPos().x + ImGui.getWindowContentRegionMax().x;
        float styleSpacingX = ImGui.getStyle().getItemSpacingX();

        for (Identifier location : entries) {
            String name = location.toString();
            if (!filter.isEmpty() && !name.contains(filter)) continue;

            ImGui.pushID(name);

            ImGui.button(location.getPath(), itemSize, itemSize);

            CataloguePayload payload = new CataloguePayload(
                    location.toString(),
                    id,
                    List.of("foundry", "editor")
            );

            if (ImGui.beginDragDropSource(ImGuiDragDropFlags.None)) {
                ImGui.setDragDropPayload("CATALOGUE_ENTRY", payload);
                ImGui.text("Placing " + id + ": " + name);
                ImGui.endDragDropSource();
            }

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip(name);
            }

            float lastButtonX2 = ImGui.getItemRectMax().x;
            float nextButtonX2 = lastButtonX2 + styleSpacingX + itemSize;

            if (nextButtonX2 < windowVisibleX2) {
                ImGui.sameLine();
            }

            ImGui.popID();
        }

        ImGui.endChild();
    }

    public record CataloguePayload(String id, String type, List<String> tags) {
    }
}