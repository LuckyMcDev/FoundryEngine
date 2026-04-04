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
import de.luckymcdev.foundryengine.common.scene.SceneZone;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
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
        if (Minecraft.getInstance().level == null) {
            ImGuiUtils.textDenied("Non existing Level",
                    "You need to join a World for this Panel to work.");
            return;
        }
        EngineSceneNode selectedNode = (selectedUUID != null) ? Common.getSceneManager().getNode(selectedUUID) : null;

        renderSearchHeader();

        float totalAvailY = ImGui.getContentRegionAvail().y;
        float spacing = ImGui.getStyle().getItemSpacingY();
        float inspectorHeight = (selectedNode != null) ? (totalAvailY * 0.40f) : 0f;
        float treeHeight = totalAvailY - inspectorHeight - (selectedNode != null ? spacing : 0);

        if (ImGui.collapsingHeader("Entities", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.beginChild("##scene_tree", 0, treeHeight, false);
            renderTree(Common.getSceneManager().getFilteredNodes(), searchBuffer.get().toLowerCase());
            ImGui.endChild();
        }

        if (selectedNode != null) {
            ImGui.separator();
            ImGui.beginChild("##inspector", 0, 0, false);
            renderInspector(selectedNode);
            ImGui.endChild();
        }
    }

    private void renderSearchHeader() {
        ImGui.setNextItemWidth(-1);
        ImGui.inputTextWithHint("##scene_search", ImIcons.FA.FA_MAGNIFYING_GLASS + "  Filter nodes...", searchBuffer);

        ImGui.textDisabled(ImIcons.FA.FA_LOCATION_DOT + " Zone:");
        ImGui.sameLine();
        String currentZoneName = "Global (All)";
        if (Common.getSceneManager().isFollowingPlayer()) {
            currentZoneName = "Current Chunk (Following)";
        } else if (Common.getSceneManager().getActiveZone() != null) {
            currentZoneName = Common.getSceneManager().getActiveZone().name();
        }

        if (ImGui.beginCombo("##zone_select", currentZoneName)) {
            if (ImGui.selectable("Global (All)", !Common.getSceneManager().isFollowingPlayer() && Common.getSceneManager().getActiveZone() == null)) {
                Common.getSceneManager().setFollowPlayer(false);
                Common.getSceneManager().setActiveZone(null);
            }

            if (ImGui.selectable("Current Chunk (Follow)", Common.getSceneManager().isFollowingPlayer())) {
                Common.getSceneManager().setFollowPlayer(true);
            }

            if (ImGui.selectable("Snapshot Current Area", false)) {
                if (Client.getPlayer() == null) return;
                var chunkPos = Client.getPlayer().chunkPosition();
                SceneZone snapshot = new SceneZone("Snapshot", chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ());
                Common.getSceneManager().setActiveZone(snapshot);
            }

            ImGui.endCombo();
        }

        ImGui.separator();
    }

    private void renderTree(Collection<EngineSceneNode> nodes, String filter) {
        if (nodes.isEmpty()) {
            ImGui.textDisabled("  No entities in scene.");
            return;
        }

        for (EngineSceneNode node : nodes) {
            if (isFiltered(node, filter)) continue;
            renderNodeEntry(node);
        }
    }

    private boolean isFiltered(EngineSceneNode node, String filter) {
        if (filter.isEmpty()) return false;
        String name = node.getDisplayName().toLowerCase();
        String type = node.getTypeName().toLowerCase();
        return !name.contains(filter) && !type.contains(filter);
    }

    private void renderNodeEntry(EngineSceneNode node) {
        String uuid = node.getUUID();
        boolean isSelected = uuid.equals(selectedUUID);

        if (isSelected) ImGui.pushStyleColor(ImGuiCol.Header, 0.26f, 0.59f, 0.98f, 0.31f);

        int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth;
        if (isSelected) flags |= ImGuiTreeNodeFlags.Selected;

        ImGui.treeNodeEx("##node_" + uuid, flags, iconForType(node.getTypeName()) + "  " + node.getDisplayName());

        if (isSelected) ImGui.popStyleColor();

        if (ImGui.isItemClicked()) selectedUUID = isSelected ? null : uuid;

        renderNodeContextMenu(node, isSelected);
        renderRightAlignedTypeText(node.getTypeName());
    }

    private void renderNodeContextMenu(EngineSceneNode node, boolean isSelected) {
        if (ImGui.beginPopupContextItem("##ctx_" + node.getUUID())) {
            ImGui.textDisabled(node.getTypeName());
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
    }

    private void renderRightAlignedTypeText(String typeName) {
        ImGui.sameLine();
        float textWidth = ImGui.calcTextSize(typeName).x;
        float posX = ImGui.getWindowWidth() - textWidth - ImGui.getStyle().getScrollbarSize() - 15f;

        if (posX > ImGui.getCursorPosX()) {
            ImGui.setCursorPosX(posX);
            ImGui.textDisabled(typeName);
        }
    }

    private void renderInspector(EngineSceneNode node) {
        renderInspectorHeader();

        if (ImGui.beginTable("##inspector_table", 2)) {
            renderProperty("Type", node.getTypeName());
            renderProperty("UUID", node.getUUID());
            renderProperty("Display", node.getDisplayName());

            Vector3f p = node.getPosition();
            renderProperty("Position", String.format("%.2f, %.2f, %.2f", p.x, p.y, p.z));

            Vector2f r = node.getRotation();
            renderProperty("Rotation", String.format("P: %.1f°, Y: %.1f°", r.x, r.y));
            ImGui.endTable();
        }

        renderInspectorActions(node);
    }

    private void renderInspectorHeader() {
        ImGui.text(ImIcons.FA.FA_CIRCLE_INFO + "  Properties");
        ImGui.sameLine();

        float btnSize = 20f;
        float posX = ImGui.getWindowWidth() - btnSize - ImGui.getStyle().getScrollbarSize() - ImGui.getStyle().getWindowPaddingX();

        if (posX > ImGui.getCursorPosX()) ImGui.setCursorPosX(posX);
        if (ImGui.smallButton(ImIcons.FA.FA_CIRCLE_XMARK.toString())) selectedUUID = null;

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
    }

    private void renderInspectorActions(EngineSceneNode node) {
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        float availX = ImGui.getContentRegionAvail().x;
        float btnWidth = (availX - ImGui.getStyle().getItemSpacingY()) * 0.5f;

        if (ImGui.button(ImIcons.FA.FA_LOCATION_CROSSHAIRS + "  Teleport", btnWidth, 0)) {
            teleportTo(node.getPosition());
        }

        ImGui.sameLine();

        ImGuiUtils.pushErrorButtonStyle();
        if (ImGui.button(ImIcons.FA.FA_TRASH + "  Remove", btnWidth, 0)) {
            node.remove();
            selectedUUID = null;
        }
        ImGuiUtils.popErrorButtonStyle();
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
        if (isMob(t)) return ImIcons.FA.FA_SKULL.toString();
        if (isAnimal(t)) return ImIcons.FA.FA_PAW.toString();
        if (t.contains("item")) return ImIcons.FA.FA_BOX.toString();
        if (isProjectile(t)) return ImIcons.FA.FA_CROSSHAIRS.toString();
        if (isVehicle(t)) return ImIcons.FA.FA_TRAIN.toString();

        return ImIcons.FA.FA_CUBE.toString();
    }

    private boolean isMob(String t) {
        return t.matches(".*(zombie|skeleton|creeper|spider|enderman|witch|phantom|blaze|wither).*");
    }

    private boolean isAnimal(String t) {
        return t.matches(".*(cow|pig|sheep|chicken|horse|wolf|cat|dog|fox|bee).*");
    }

    private boolean isProjectile(String t) {
        return t.matches(".*(arrow|projectile|fireball).*");
    }

    private boolean isVehicle(String t) {
        return t.matches(".*(boat|minecart).*");
    }
}