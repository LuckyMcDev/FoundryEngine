package de.luckymcdev.foundryengine.client.editor.builtin.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Collection;

public class ScenePanel extends EditorPanel {
    public static final ScenePanel INSTANCE = new ScenePanel();

    private final ImString searchBuffer = new ImString(256);
    private String selectedUUID = null;

    public ScenePanel() {
        super(Common.id("scene"), "Scene", ImIcons.FA.FA_FOLDER_TREE, Shortcut.empty());
        this.category = PanelCategory.EDITOR_SCENE;
    }

    @Override
    public void content() {
        if (!ImGuiUtils.requireFull()) return;

        Collection<EngineSceneNode> nodes = Common.getSceneManager().getNodes();
        EngineSceneNode selectedNode = null;
        if (selectedUUID != null) {
            selectedNode = Common.getSceneManager().getNode(selectedUUID);
        }

        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("##scene_search", ImIcons.FA.FA_MAGNIFYING_GLASS + "  Filter nodes...", searchBuffer);
        ImGui.separator();

        float totalAvailY = ImGui.getContentRegionAvail().y;
        float spacing = ImGui.getStyle().getItemSpacingY();

        float inspectorHeight = (selectedNode != null) ? (totalAvailY * 0.40f) : 0f;
        float treeHeight = totalAvailY - inspectorHeight - (selectedNode != null ? spacing : 0);

        if (ImGui.collapsingHeader("Entities", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.beginChild("##scene_tree", 0, treeHeight, false);
            renderTree(nodes, searchBuffer.get().toLowerCase());
            ImGui.endChild();
        }

        if (selectedNode != null) {
            ImGui.separator();
            ImGui.beginChild("##inspector", 0, 0, false);
            renderInspector(selectedNode);
            ImGui.endChild();
        }
    }

    private void renderTree(Collection<EngineSceneNode> nodes, String filter) {
        if (nodes.isEmpty()) {
            ImGui.textDisabled("  No entities in scene.");
            return;
        }

        for (EngineSceneNode node : nodes) {
            String uuid = node.getUUID();
            String displayName = node.getDisplayName();
            String typeName = node.getTypeName();

            if (!filter.isEmpty() && !displayName.toLowerCase().contains(filter) && !typeName.toLowerCase().contains(filter)) {
                continue;
            }

            boolean isSelected = uuid.equals(selectedUUID);
            int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth;
            if (isSelected) flags |= ImGuiTreeNodeFlags.Selected;

            if (isSelected) ImGui.pushStyleColor(ImGuiCol.Header, 0.26f, 0.59f, 0.98f, 0.31f);
            ImGui.treeNodeEx("##node_" + uuid, flags, iconForType(typeName) + "  " + displayName);
            if (isSelected) ImGui.popStyleColor();

            if (ImGui.isItemClicked()) {
                selectedUUID = isSelected ? null : uuid;
            }

            if (ImGui.beginPopupContextItem("##ctx_" + uuid)) {
                ImGui.textDisabled(typeName);
                ImGui.separator();
                if (ImGui.menuItem(ImIcons.FA.FA_LOCATION_CROSSHAIRS + "  Teleport to")) {
                    teleportTo(node.getPosition());
                }
                if (ImGui.menuItem(ImIcons.FA.FA_TRASH + "  Remove")) {
                    node.remove();
                    if (isSelected) selectedUUID = null;
                }
                ImGui.endPopup();
            }

            ImGui.sameLine();
            float textWidth = ImGui.calcTextSize(typeName).x;
            float posX = ImGui.getWindowWidth() - textWidth - ImGui.getStyle().getScrollbarSize() - 15f;
            if (posX > ImGui.getCursorPosX()) {
                ImGui.setCursorPosX(posX);
                ImGui.textDisabled(typeName);
            }
        }
    }

    private void renderInspector(EngineSceneNode node) {
        ImGui.text(ImIcons.FA.FA_CIRCLE_INFO + "  Properties");

        ImGui.sameLine();
        float buttonSize = 20f;
        float closeBtnPosX = ImGui.getWindowWidth() - buttonSize - ImGui.getStyle().getScrollbarSize() - ImGui.getStyle().getWindowPaddingX();

        if (closeBtnPosX > ImGui.getCursorPosX()) {
            ImGui.setCursorPosX(closeBtnPosX);
        }

        if (ImGui.smallButton(ImIcons.FA.FA_CIRCLE_XMARK.toString())) {
            selectedUUID = null;
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        if (ImGui.beginTable("##inspector_table", 2)) {
            renderProperty("Type", node.getTypeName());
            renderProperty("UUID", node.getUUID());
            renderProperty("Display", node.getDisplayName());

            Vector3f pos = node.getPosition();
            renderProperty("Position", String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));

            Vector2f rot = node.getRotation();
            renderProperty("Rotation", String.format("P: %.1f°, Y: %.1f°", rot.x, rot.y));

            ImGui.endTable();
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        float availX = ImGui.getContentRegionAvail().x;
        float btnWidth = (availX - ImGui.getStyle().getItemSpacingX()) * 0.5f;

        if (ImGui.button(ImIcons.FA.FA_LOCATION_CROSSHAIRS + "  Teleport", btnWidth, 0)) {
            teleportTo(node.getPosition());
        }

        ImGui.sameLine();

        ImGui.pushStyleColor(ImGuiCol.Button, 0.6f, 0.1f, 0.1f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.8f, 0.2f, 0.2f, 1.0f);
        if (ImGui.button(ImIcons.FA.FA_TRASH + "  Remove", btnWidth, 0)) {
            node.remove();
            selectedUUID = null;
        }
        ImGui.popStyleColor(2);
    }

    private void renderProperty(String label, String value) {
        ImGui.tableNextColumn();
        ImGui.textDisabled(label);
        ImGui.tableNextColumn();
        ImGui.text(value);
    }

    private void teleportTo(Vector3f pos) {
        if (Client.getConnection() != null) {
            Client.getConnection().send(new ServerBoundTeleportPacket(pos));
        }
    }

    private String iconForType(String typeName) {
        if (typeName == null) return ImIcons.FA.FA_CUBE.toString();
        String t = typeName.toLowerCase();
        if (t.contains("player")) return ImIcons.FA.FA_PERSON.toString();
        if (t.matches(".*(zombie|skeleton|creeper|spider|enderman|witch|phantom|blaze|wither).*"))
            return ImIcons.FA.FA_SKULL.toString();
        if (t.matches(".*(cow|pig|sheep|chicken|horse|wolf|cat|dog|fox|bee).*")) return ImIcons.FA.FA_PAW.toString();
        if (t.contains("item")) return ImIcons.FA.FA_BOX.toString();
        if (t.matches(".*(arrow|projectile|fireball).*")) return ImIcons.FA.FA_CROSSHAIRS.toString();
        if (t.matches(".*(boat|minecart).*")) return ImIcons.FA.FA_TRAIN.toString();
        return ImIcons.FA.FA_CUBE.toString();
    }
}