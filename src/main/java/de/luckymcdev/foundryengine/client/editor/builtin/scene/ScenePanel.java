package de.luckymcdev.foundryengine.client.editor.builtin.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.scene.ClientSceneSync;
import de.luckymcdev.foundryengine.client.scene.SceneSelectionManager;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.packets.ServerBoundTeleportPacket;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import de.luckymcdev.foundryengine.common.scene.PersistedSceneNode;
import de.luckymcdev.foundryengine.common.scene.WorldEntitySceneNode;
import de.luckymcdev.foundryengine.common.util.NamedAABB;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

public class ScenePanel extends EditorPanel {
    public static final ScenePanel INSTANCE = new ScenePanel();

    private final ImString searchBuffer = new ImString(256);
    private final ImString createEmptyNameBuffer = new ImString(128);
    private final ImString renameBuffer = new ImString(128);
    public boolean showGizmos = true;
    private boolean showWorldEntities = true;
    private String selectedUUID = null;

    private NamedAABB activeZone;
    private boolean followPlayer = false;

    private String renameTargetUuid = null;

    public ScenePanel() {
        super(Common.id("scene"), "Scene", ImIcons.FA.FA_FOLDER_TREE, Shortcut.empty());
        this.category = PanelCategory.EDITOR_SCENE;
    }

    @Override
    public void content() {
        if (!ImGuiUtils.requireWorld("You need to join a world for this panel to work.")) {
            return;
        }

        EngineSceneNode selectedNode = ClientSceneSync.findNode(selectedUUID);

        renderToolbar();
        renderSearchHeader();
        renderRenamePopup();
        renderCreateEmptyPopup();

        float totalAvailY = ImGui.getContentRegionAvail().y;
        float spacing = ImGui.getStyle().getItemSpacingY();
        float inspectorHeight = (selectedNode != null) ? (totalAvailY * 0.38f) : 0f;
        float treeHeight = totalAvailY - inspectorHeight - (selectedNode != null ? spacing * 2 : 0);

        ImGui.beginChild("##scene_tree", 0, treeHeight, false);
        renderTree(searchBuffer.get().toLowerCase());
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

        String entitiesLabel = (showWorldEntities ? ImIcons.FA.FA_USERS : ImIcons.FA.FA_USER_SLASH) + "  Entities";
        if (ImGui.button(entitiesLabel)) {
            showWorldEntities = !showWorldEntities;
        }
        ImGui.sameLine();

        if (ImGui.button(ImIcons.FA.FA_PLUS + "  Empty")) {
            createEmptyNameBuffer.set("Empty");
            ImGui.openPopup("###create-empty-node");
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
        if (followPlayer) {
            currentZoneName = "Current Chunk (Following)";
        } else if (activeZone != null) {
            currentZoneName = activeZone.name;
        }

        if (ImGui.beginCombo("##zone_select", currentZoneName)) {
            if (ImGui.selectable("Global (All)", !followPlayer && activeZone == null)) {
                followPlayer = false;
                activeZone = null;
            }
            if (ImGui.selectable("Current Chunk (Follow)", followPlayer)) {
                followPlayer = true;
                activeZone = null;
            }
            if (ImGui.selectable("Snapshot Current Area", false)) {
                if (Client.getPlayer() == null) return;
                var chunkPos = Client.getPlayer().chunkPosition();
                var level = Minecraft.getInstance().level;
                activeZone = new NamedAABB("Snapshot",
                        chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                        chunkPos.getMaxBlockX(), level.getMinY() + level.getHeight(), chunkPos.getMaxBlockZ()
                );
                followPlayer = false;
            }
            ImGui.endCombo();
        }

        ImGui.separator();
    }

    private void renderTree(String filter) {
        Collection<? extends EngineSceneNode> persistedRoots = filteredRoots(Common.getSceneManager().getClientGraph().getRoots());

        int rootFlags = ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen;
        boolean sceneOpen = ImGui.treeNodeEx("##persisted_scene_root", rootFlags, ImIcons.FA.FA_FOLDER_TREE + "  Scene");
        if (sceneOpen) {
            renderGroupedTree(new ArrayList<>(persistedRoots), filter);
            ImGui.treePop();
        }

        if (showWorldEntities) {
            boolean entOpen = ImGui.treeNodeEx("##world_entities_root", rootFlags, ImIcons.FA.FA_USERS + "  World Entities");
            if (entOpen) {
                renderGroupedTree(buildWorldEntityRoots(), filter);
                ImGui.treePop();
            }
        }
    }

    private Collection<EngineSceneNode> buildWorldEntityRoots() {
        var level = Minecraft.getInstance().level;
        if (level == null) return List.of();

        ArrayList<EngineSceneNode> roots = new ArrayList<>();
        for (var e : level.entitiesForRendering()) {
            if (e == null) continue;
            roots.add(new WorldEntitySceneNode(e));
        }
        return filteredRoots(roots);
    }

    private <T extends EngineSceneNode> Collection<T> filteredRoots(Collection<T> input) {
        ArrayList<T> filtered = new ArrayList<>(input);

        if (followPlayer) {
            var player = Client.getPlayer();
            if (player != null) {
                ChunkPos chunk = player.chunkPosition();
                filtered.removeIf(n -> !isInChunk(n, chunk));
            }
        } else if (activeZone != null) {
            filtered.removeIf(n -> !activeZone.contains(n.getPosition().x, 0, n.getPosition().z));
        }

        return filtered;
    }

    private boolean isInChunk(EngineSceneNode node, ChunkPos chunk) {
        float x = node.getPosition().x;
        float z = node.getPosition().z;
        return x >= chunk.getMinBlockX() && x <= chunk.getMaxBlockX()
                && z >= chunk.getMinBlockZ() && z <= chunk.getMaxBlockZ();
    }

    private void renderGroupedTree(Collection<EngineSceneNode> roots, String filter) {
        if (roots.isEmpty()) {
            ImGui.textDisabled("  No nodes.");
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
            ImGui.textDisabled("  No matching nodes.");
            return;
        }

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
            SceneSelectionManager.setSelected(node);
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
            boolean isPersisted = node instanceof PersistedSceneNode;

            if (isPersisted && ImGui.menuItem(ImIcons.FA.FA_PEN + "  Rename")) {
                renameTargetUuid = node.getUUID();
                renameBuffer.set(node.getDisplayName());
                ImGui.closeCurrentPopup();
                ImGui.openPopup("###rename-node");
            }

            if (node.editable() && ImGui.menuItem(ImIcons.FA.FA_TRASH + "  Remove")) {
                node.remove();
                if (isPersisted) {
                    maybePushGraph();
                }
                if (isSelected) {
                    selectedUUID = null;
                    SceneSelectionManager.setSelected(null);
                }
            }
            ImGui.endPopup();
        }
    }

    private void renderRenamePopup() {
        if (ImGui.beginPopupModal("###rename-node", new ImBoolean(true), imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Name:");
            ImGui.setNextItemWidth(260f);
            ImGui.inputText("##rename", renameBuffer);

            if (ImGui.button("OK##rename", 80, 0)) {
                String newName = renameBuffer.get().trim();
                if (renameTargetUuid != null && !newName.isEmpty()) {
                    var node = Common.getSceneManager().getClientGraph().getNode(renameTargetUuid);
                    if (node != null) {
                        node.setDisplayName(newName);
                        maybePushGraph();
                    }
                }
                renameTargetUuid = null;
                renameBuffer.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("Cancel##rename", 80, 0)) {
                renameTargetUuid = null;
                renameBuffer.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void renderCreateEmptyPopup() {
        if (ImGui.beginPopupModal("###create-empty-node", new ImBoolean(true), imgui.flag.ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Name:");
            ImGui.setNextItemWidth(260f);
            ImGui.inputText("##create-empty-name", createEmptyNameBuffer);

            if (ImGui.button("Create##empty", 90, 0)) {
                String name = createEmptyNameBuffer.get().trim();
                if (name.isEmpty()) name = "Empty";
                createEmptyNode(name);
                createEmptyNameBuffer.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.sameLine();
            if (ImGui.button("Cancel##empty", 90, 0)) {
                createEmptyNameBuffer.set("");
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void createEmptyNode(String name) {
        var player = Client.getPlayer();
        if (player == null) return;
        Vector3f pos = player.position().toVector3f();
        Vector2f rot = new Vector2f(player.getXRot(), player.getYRot());
        createNode("foundryengine:empty", name, pos, rot);
    }

    private void createNode(String typeName, String name, Vector3f pos, Vector2f rot) {
        var graph = Common.getSceneManager().getClientGraph();
        var node = graph.createNode(typeName, name, pos, rot, null);
        ClientSceneSync.pushToServer(graph);

        selectedUUID = node.getUUID();
        SceneSelectionManager.setSelected(node);
    }

    private void maybePushGraph() {
        var graph = Common.getSceneManager().getClientGraph();
        if (graph.isDirty()) {
            ClientSceneSync.pushToServer(graph);
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

        Vector3f pos = node.getLocalPosition();
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
                node.setLocalPosition(newPos);
                if (node instanceof PersistedSceneNode) maybePushGraph();
            }
        } else {
            ImGui.textDisabled(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
        }
    }

    private void renderRotationSliders(EngineSceneNode node) {
        ImGui.tableNextColumn();
        ImGui.text("Rotation");
        ImGui.tableNextColumn();

        Vector2f rot = node.getLocalRotation();
        boolean editable = node.editable();

        if (editable) {
            float[] yaw = {rot.x};
            float[] pitch = {rot.y};
            boolean changed = false;

            ImGui.setNextItemWidth(-1);
            if (ImGui.sliderFloat("Yaw", yaw, -180.0f, 180.0f, "%.2f")) changed = true;
            if (ImGui.sliderFloat("Pitch", pitch, -180.0f, 180.0f, "%.2f")) changed = true;

            if (changed) {
                Vector2f newRot = new Vector2f(yaw[0], pitch[0]);
                node.setLocalRotation(newRot);
                if (node instanceof PersistedSceneNode) maybePushGraph();
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
                if (node instanceof PersistedSceneNode) maybePushGraph();
            }
        } else if (value instanceof Integer) {
            ImInt val = new ImInt((Integer) value);
            if (ImGui.inputInt("##" + key, val)) {
                node.setProperty(key, val.get());
                if (node instanceof PersistedSceneNode) maybePushGraph();
            }
        } else if (value instanceof Boolean) {
            ImBoolean val = new ImBoolean((Boolean) value);
            if (ImGui.checkbox("##" + key, val)) {
                node.setProperty(key, val.get());
                if (node instanceof PersistedSceneNode) maybePushGraph();
            }
        } else if (value instanceof String) {
            ImString val = new ImString((String) value, 256);
            if (ImGui.inputText("##" + key, val)) {
                node.setProperty(key, val.get());
                if (node instanceof PersistedSceneNode) maybePushGraph();
            }
        } else {
            ImGui.textDisabled(value.toString());
        }
    }

    private String getNodeCategory(EngineSceneNode node) {
        if (node instanceof PersistedSceneNode) return "Scene";
        if (node instanceof WorldEntitySceneNode) return "Entities";
        return "Other";
    }

    private ImIcon getCategoryIcon(String category) {
        return switch (category) {
            case "Scene" -> ImIcons.FA.FA_CUBE;
            case "Entities" -> ImIcons.FA.FA_USERS;
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
