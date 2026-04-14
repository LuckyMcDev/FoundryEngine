package de.luckymcdev.foundryengine.client.editor.builtin.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.scene.SelectionManager;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import de.luckymcdev.foundryengine.common.scene.SceneZone;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

import java.util.Collection;

public class ScenePanel extends EditorPanel {
    public static final ScenePanel INSTANCE = new ScenePanel();

    private final ImString searchBuffer = new ImString(256);
    public boolean showGizmos = true;
    private String selectedUUID = null;

    public ScenePanel() {
        super(Common.id("scene"), "Scene", ImIcons.FA.FA_FOLDER_TREE, Shortcut.empty());
        this.category = PanelCategory.EDITOR_SCENE;
    }

    @Override
    public void content() {
        if (Minecraft.getInstance().level == null) {
            ImGuiUtils.textDenied("Non existing Level", "You need to join a World for this Panel to work.");
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
            renderTree(Common.getSceneManager().getFilteredRoots(), searchBuffer.get().toLowerCase());
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

    private void renderTree(Collection<EngineSceneNode> roots, String filter) {
        if (roots.isEmpty()) {
            ImGui.textDisabled("  No entities in scene.");
            return;
        }

        boolean anyVisible = false;
        for (EngineSceneNode root : roots) {
            anyVisible |= renderNodeRecursive(root, filter);
        }

        if (!anyVisible) {
            ImGui.textDisabled("  No matching entities.");
        }
    }

    private boolean renderNodeRecursive(EngineSceneNode node, String filter) {
        boolean matchesFilter = !isFiltered(node, filter);
        boolean childMatches = false;

        for (EngineSceneNode child : node.getChildren()) {
            if (renderNodeRecursive(child, filter)) {
                childMatches = true;
            }
        }

        if (!matchesFilter && !childMatches) {
            return false;
        }

        boolean hasChildren = !node.getChildren().isEmpty();
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        if (!hasChildren) flags |= ImGuiTreeNodeFlags.Leaf;
        if (node.getUUID().equals(selectedUUID)) flags |= ImGuiTreeNodeFlags.Selected;
        if (!filter.isEmpty() && (matchesFilter || childMatches)) flags |= ImGuiTreeNodeFlags.DefaultOpen;

        boolean open = ImGui.treeNodeEx("##node_" + node.getUUID(), flags,
                iconForType(node.getTypeName()) + "  " + node.getDisplayName());

        if (ImGui.isItemClicked()) {
            selectedUUID = node.getUUID();
            SelectionManager.setSelected(node);
        }

        renderNodeContextMenu(node, node.getUUID().equals(selectedUUID));

        if (open) {
            for (EngineSceneNode child : node.getChildren()) {
                renderNodeRecursive(child, filter);
            }
            ImGui.treePop();
        }

        return true;
    }

    private boolean isFiltered(EngineSceneNode node, String filter) {
        if (filter.isEmpty()) return false;
        String name = node.getDisplayName().toLowerCase();
        String type = node.getTypeName().toLowerCase();
        return !name.contains(filter) && !type.contains(filter);
    }

    private void renderNodeContextMenu(EngineSceneNode node, boolean isSelected) {
        if (ImGui.beginPopupContextItem("##ctx_" + node.getUUID())) {
            ImGui.textDisabled(node.getTypeName());
            ImGui.separator();

            if (ImGui.menuItem(ImIcons.FA.FA_CAMERA + "  Set as Camera")) {
                var mc = Client.getMinecraft();
                if (mc.getCameraEntity() == node.asEntity()) {
                    mc.setCameraEntity(null);
                } else {
                    mc.setCameraEntity(node.asEntity());
                }
            }
            if (ImGui.menuItem(ImIcons.FA.FA_LOCATION_CROSSHAIRS + "  Teleport to")) {
                teleportTo(node.getPosition());
            }
            if (ImGui.menuItem(ImIcons.FA.FA_TRASH + "  Remove")) {
                node.remove();
                if (isSelected) {
                    selectedUUID = null;
                    SelectionManager.setSelected(null);
                }
            }
            ImGui.endPopup();
        }
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

    public String getSelectedUUID() {
        return selectedUUID;
    }
}