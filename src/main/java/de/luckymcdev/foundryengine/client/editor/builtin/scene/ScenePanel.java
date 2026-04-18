package de.luckymcdev.foundryengine.client.editor.builtin.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.scene.SelectionManager;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import de.luckymcdev.foundryengine.common.scene.EntitySceneNode;
import de.luckymcdev.foundryengine.common.scene.PointNode;
import de.luckymcdev.foundryengine.common.scene.SceneZone;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

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

        renderToolbar();
        renderSearchHeader();

        float totalAvailY = ImGui.getContentRegionAvail().y;
        float spacing = ImGui.getStyle().getItemSpacingY();
        float inspectorHeight = (selectedNode != null) ? (totalAvailY * 0.38f) : 0f;
        float treeHeight = totalAvailY - inspectorHeight - (selectedNode != null ? spacing * 2 : 0);

        ImGui.beginChild("##scene_tree", 0, treeHeight, false);
        renderGroupedTree(Common.getSceneManager().getFilteredRoots(), searchBuffer.get().toLowerCase());
        ImGui.endChild();

        if (selectedNode != null) {
            ImGui.separator();
            ImGui.beginChild("##scene_inspector", 0, inspectorHeight, false);
            renderInspector(selectedNode);
            ImGui.endChild();
        }
    }

    private void renderToolbar() {
        String gizmoLabel = (showGizmos ? ImIcons.FA.FA_EYE : ImIcons.FA.FA_EYE_SLASH) + "  Gizmos";
        if (ImGui.button(gizmoLabel)) {
            showGizmos = !showGizmos;
        }
        ImGui.sameLine();
        ImGui.separator();
        ImGui.sameLine();
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

    private void renderGroupedTree(Collection<EngineSceneNode> roots, String filter) {
        if (roots.isEmpty()) {
            ImGui.textDisabled("  No entities in scene.");
            return;
        }

        Map<String, Map<String, List<EngineSceneNode>>> categoryMap = new LinkedHashMap<>();
        for (EngineSceneNode node : roots) {
            if (nodeOrChildMatchesFilter(node, filter)) {
                String category = getNodeCategory(node);
                String type = node.getTypeName();
                categoryMap.computeIfAbsent(category, k -> new LinkedHashMap<>())
                        .computeIfAbsent(type, k -> new ArrayList<>())
                        .add(node);
            }
        }

        if (categoryMap.isEmpty()) {
            ImGui.textDisabled("  No matching entities.");
            return;
        }

        int rootFlags = ImGuiTreeNodeFlags.SpanAvailWidth;
        boolean rootOpen = ImGui.treeNodeEx("##scene_root", rootFlags, ImIcons.FA.FA_FOLDER_TREE + "  Scene");
        if (rootOpen) {
            for (Map.Entry<String, Map<String, List<EngineSceneNode>>> catEntry : categoryMap.entrySet()) {
                String category = catEntry.getKey();
                Map<String, List<EngineSceneNode>> typeMap = catEntry.getValue();

                int catFlags = ImGuiTreeNodeFlags.SpanAvailWidth;
                if (!filter.isEmpty()) catFlags |= ImGuiTreeNodeFlags.DefaultOpen;
                String catLabel = getCategoryIcon(category) + "  " + category + "  (" + typeMap.size() + ")";
                boolean catOpen = ImGui.treeNodeEx("##cat_" + category, catFlags, catLabel);
                if (catOpen) {
                    for (Map.Entry<String, List<EngineSceneNode>> typeEntry : typeMap.entrySet()) {
                        renderTypeGroup(typeEntry.getKey(), typeEntry.getValue(), filter);
                    }
                    ImGui.treePop();
                }
            }
            ImGui.treePop();
        }
    }

    private void renderTypeGroup(String typeName, List<EngineSceneNode> nodes, String filter) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        if (!filter.isEmpty()) flags |= ImGuiTreeNodeFlags.DefaultOpen;

        String header = iconForType(typeName) + "  " + friendlyTypeName(typeName) + "  (" + nodes.size() + ")";
        boolean open = ImGui.treeNodeEx("##type_" + typeName, flags, header);
        if (open) {
            for (EngineSceneNode node : nodes) {
                renderNodeRecursive(node, filter);
            }
            ImGui.treePop();
        }
    }

    private void renderNodeRecursive(EngineSceneNode node, String filter) {
        if (!nodeOrChildMatchesFilter(node, filter)) return;

        boolean hasChildren = !node.getChildren().isEmpty();
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        if (!hasChildren) flags |= ImGuiTreeNodeFlags.Leaf;
        if (node.getUUID().equals(selectedUUID)) flags |= ImGuiTreeNodeFlags.Selected;
        if (!filter.isEmpty()) flags |= ImGuiTreeNodeFlags.DefaultOpen;

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
    }

    private boolean nodeOrChildMatchesFilter(EngineSceneNode node, String filter) {
        if (filter.isEmpty()) return true;
        if (matchesFilter(node, filter)) return true;
        for (EngineSceneNode child : node.getChildren()) {
            if (nodeOrChildMatchesFilter(child, filter)) return true;
        }
        return false;
    }

    private boolean matchesFilter(EngineSceneNode node, String filter) {
        return node.getDisplayName().toLowerCase().contains(filter)
                || node.getTypeName().toLowerCase().contains(filter);
    }

    private void renderNodeContextMenu(EngineSceneNode node, boolean isSelected) {
        if (ImGui.beginPopupContextItem("##ctx_" + node.getUUID())) {
            ImGui.textDisabled(node.getTypeName());
            ImGui.separator();

            if (ImGui.menuItem(ImIcons.FA.FA_CAMERA + "  Set as Camera")) {
                var mc = Client.getMc();
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

    private void renderInspector(EngineSceneNode node) {
        ImGui.text(ImIcons.FA.FA_SLIDERS + "  " + node.getDisplayName());
        ImGui.sameLine();
        ImGui.textDisabled("(" + node.getTypeName() + ")");
        ImGui.separator();

        if (ImGui.beginTable("##props_table", 2)) {
            renderStaticProperty("UUID", node.getUUID());
            renderStaticProperty("Type", node.getTypeName());
            renderReadOnlyDisplayName(node);
            renderPositionSliders(node);
            renderRotationSliders(node);

            Map<String, Object> props = node.getProperties();
            if (!props.isEmpty()) {
                ImGui.tableNextColumn();
                ImGui.separator();
                ImGui.tableNextColumn();
                ImGui.separator();

                for (Map.Entry<String, Object> entry : props.entrySet()) {
                    renderCustomProperty(node, entry.getKey(), entry.getValue());
                }
            }

            ImGui.endTable();
        }
    }

    private void renderStaticProperty(String label, String value) {
        ImGui.tableNextColumn();
        ImGui.textDisabled(label);
        ImGui.tableNextColumn();
        ImGui.text(value);
    }

    private void renderReadOnlyDisplayName(EngineSceneNode node) {
        ImGui.tableNextColumn();
        ImGui.textDisabled("Display Name");
        ImGui.tableNextColumn();
        ImGui.text(node.getDisplayName());
    }

    private void renderPositionSliders(EngineSceneNode node) {
        ImGui.tableNextColumn();
        ImGui.text("Position");
        ImGui.tableNextColumn();

        Vector3f pos = node.getPosition();
        boolean editable = node.editable();

        if (editable) {
            float[] x = {pos.x};
            float[] y = {pos.y};
            float[] z = {pos.z};

            float min = -500.0f;
            float max = 500.0f;

            ImGui.setNextItemWidth(-1);
            boolean changed = ImGui.sliderFloat("X", x, min, max, "%.2f");
            if (ImGui.sliderFloat("Y", y, min, max, "%.2f")) changed = true;
            if (ImGui.sliderFloat("Z", z, min, max, "%.2f")) changed = true;

            if (changed) {
                Vector3f newPos = new Vector3f(x[0], y[0], z[0]);
                if (node instanceof PointNode point) {
                    point.setPosition(newPos);
                } else if (node instanceof EntitySceneNode entityNode) {
                    var entity = entityNode.asEntity();
                    if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
                        Vector2f rot = node.getRotation();
                        entity.teleportTo(serverLevel, newPos.x, newPos.y, newPos.z,
                                Set.of(), rot.x, rot.y, false);
                    }
                }
            }
        } else {
            ImGui.textDisabled(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
        }
    }

    private void renderRotationSliders(EngineSceneNode node) {
        ImGui.tableNextColumn();
        ImGui.text("Rotation");
        ImGui.tableNextColumn();

        Vector2f rot = node.getRotation();
        boolean editable = (node instanceof PointNode) || (node instanceof EntitySceneNode);

        if (editable) {
            float[] yaw = {rot.x};
            float[] pitch = {rot.y};
            boolean changed = false;

            ImGui.setNextItemWidth(-1);
            if (ImGui.sliderFloat("Yaw", yaw, -180.0f, 180.0f, "%.2f")) changed = true;
            if (ImGui.sliderFloat("Pitch", pitch, -180.0f, 180.0f, "%.2f")) changed = true;

            if (changed) {
                Vector2f newRot = new Vector2f(yaw[0], pitch[0]);
                if (node instanceof PointNode point) {
                    point.setRotation(newRot);
                } else if (node instanceof EntitySceneNode entityNode) {
                    var entity = entityNode.asEntity();
                    if (entity != null && entity.level() instanceof ServerLevel serverLevel) {
                        Vector3f pos = node.getPosition();
                        entity.teleportTo(serverLevel, pos.x, pos.y, pos.z,
                                Set.of(), newRot.x, newRot.y, false);
                    }
                }
            }
        } else {
            ImGui.textDisabled(String.format("%.2f, %.2f", rot.x, rot.y));
        }
    }

    private void renderCustomProperty(EngineSceneNode node, String key, Object value) {
        ImGui.tableNextColumn();
        ImGui.text(key);
        ImGui.tableNextColumn();
        ImGui.setNextItemWidth(-1);

        if (value instanceof Float) {
            ImFloat val = new ImFloat((Float) value);
            if (ImGui.inputFloat("##" + key, val)) {
                node.setProperty(key, val.get());
            }
        } else if (value instanceof Integer) {
            ImInt val = new ImInt((Integer) value);
            if (ImGui.inputInt("##" + key, val)) {
                node.setProperty(key, val.get());
            }
        } else if (value instanceof Boolean) {
            ImBoolean val = new ImBoolean((Boolean) value);
            if (ImGui.checkbox("##" + key, val)) {
                node.setProperty(key, val.get());
            }
        } else if (value instanceof String) {
            ImString val = new ImString((String) value, 256);
            if (ImGui.inputText("##" + key, val)) {
                node.setProperty(key, val.get());
            }
        } else {
            ImGui.textDisabled(value.toString());
        }
    }

    private String getNodeCategory(EngineSceneNode node) {
        if (node instanceof EntitySceneNode) return "Entities";
        if (node instanceof PointNode) return "Points";
        return "Other";
    }

    private ImIcon getCategoryIcon(String category) {
        return switch (category) {
            case "Entities" -> ImIcons.FA.FA_USERS;
            case "Points" -> ImIcons.FA.FA_LOCATION_DOT;
            default -> ImIcons.FA.FA_CUBE;
        };
    }

    private void teleportTo(Vector3f pos) {
        if (Client.getConnection() != null) {
            Client.getConnection().send(new ServerBoundTeleportPacket(pos));
        }
    }

    private String friendlyTypeName(String typeName) {
        if (typeName == null) return "Unknown";
        int colon = typeName.lastIndexOf(':');
        String raw = (colon >= 0) ? typeName.substring(colon + 1) : typeName;
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).replace('_', ' ');
    }

    private ImIcon iconForType(String typeName) {
        if (typeName == null) return ImIcons.FA.FA_CUBE;
        String t = typeName.toLowerCase();

        if (t.contains("player")) return ImIcons.FA.FA_PERSON;
        if (t.matches(".*(zombie|skeleton|creeper|spider|enderman|witch|phantom|blaze|wither).*"))
            return ImIcons.FA.FA_SKULL;
        if (t.matches(".*(cow|pig|sheep|chicken|horse|wolf|cat|dog|fox|bee).*")) return ImIcons.FA.FA_PAW;
        if (t.contains("item")) return ImIcons.FA.FA_BOX;
        if (t.matches(".*(arrow|projectile|fireball).*")) return ImIcons.FA.FA_CROSSHAIRS;
        if (t.matches(".*(boat|minecart).*")) return ImIcons.FA.FA_TRAIN;
        if (t.contains("point")) return ImIcons.FA.FA_LOCATION_DOT;

        return ImIcons.FA.FA_CUBE;
    }

    public String getSelectedUUID() {
        return selectedUUID;
    }
}