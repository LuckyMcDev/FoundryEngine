package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Persisted scene graph (dimension-local).
 *
 * <p>Nodes are stored by UUID, with parenting forming the hierarchy.</p>
 */
public final class SceneGraph {
    public static final int FORMAT_VERSION = 1;

    private final Map<String, PersistedSceneNode> nodes = new LinkedHashMap<>();
    private final List<PersistedSceneNode> roots = new ArrayList<>();

    private boolean dirty = false;

    public static SceneGraph fromNbt(@Nullable CompoundTag tag) {
        SceneGraph graph = new SceneGraph();
        if (tag == null) return graph;

        ListTag nodeList = tag.getListOrEmpty("Nodes");

        // 1) Create all nodes.
        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag n = nodeList.getCompoundOrEmpty(i);
            String uuid = n.getStringOr("Uuid", "");
            if (uuid == null || uuid.isBlank()) continue;
            String name = n.getStringOr("Name", "Node");
            String type = n.getStringOr("Type", "foundryengine:node");
            PersistedSceneNode node = new PersistedSceneNode(graph, uuid, name, type);

            Vector3f pos = getVec3(n, "LocalPos", new Vector3f());
            Vector2f rot = getVec2(n, "LocalRot", new Vector2f());
            node.setLocalPosition(pos);
            node.setLocalRotation(rot);

            readPropsInto(node, n.getListOrEmpty("Props"));

            graph.nodes.put(uuid, node);
        }

        // 2) Wire parents/children.
        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag n = nodeList.getCompoundOrEmpty(i);
            String uuid = n.getStringOr("Uuid", "");
            if (uuid == null || uuid.isBlank()) continue;

            PersistedSceneNode node = graph.nodes.get(uuid);
            if (node == null) continue;

            String parentUuid = n.getStringOr("Parent", "");
            if (parentUuid != null && !parentUuid.isBlank()) {
                PersistedSceneNode parent = graph.nodes.get(parentUuid);
                if (parent != null && parent != node) {
                    parent.addChild(node);
                    continue;
                }
            }
            graph.roots.add(node);
        }

        graph.dirty = false;
        return graph;
    }

    private static void writeProps(CompoundTag nodeTag, Map<String, Object> props) {
        if (props == null || props.isEmpty()) return;

        ListTag list = new ListTag();
        for (var e : props.entrySet()) {
            if (e.getKey() == null || e.getKey().isBlank()) continue;

            CompoundTag p = new CompoundTag();
            p.putString("Key", e.getKey());

            Object v = e.getValue();
            if (v instanceof Integer i) {
                p.putString("Type", "int");
                p.putInt("I", i);
            } else if (v instanceof Float f) {
                p.putString("Type", "float");
                p.putFloat("F", f);
            } else if (v instanceof Double d) {
                p.putString("Type", "float");
                p.putFloat("F", d.floatValue());
            } else if (v instanceof Boolean b) {
                p.putString("Type", "bool");
                p.putBoolean("B", b);
            } else {
                p.putString("Type", "string");
                p.putString("S", v != null ? v.toString() : "");
            }

            list.add(p);
        }

        nodeTag.put("Props", list);
    }

    private static void readPropsInto(PersistedSceneNode node, ListTag list) {
        if (list == null || list.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            CompoundTag p = list.getCompoundOrEmpty(i);
            String key = p.getStringOr("Key", "");
            if (key == null || key.isBlank()) continue;

            String type = p.getStringOr("Type", "string");
            Object value = switch (type) {
                case "int" -> p.getIntOr("I", 0);
                case "float" -> p.getFloatOr("F", 0f);
                case "bool" -> p.getBooleanOr("B", false);
                default -> p.getStringOr("S", "");
            };

            node.setProperty(key, value);
        }
    }

    private static void putVec3(CompoundTag tag, String key, Vector3f v) {
        ListTag list = new ListTag();
        list.add(net.minecraft.nbt.FloatTag.valueOf(v.x));
        list.add(net.minecraft.nbt.FloatTag.valueOf(v.y));
        list.add(net.minecraft.nbt.FloatTag.valueOf(v.z));
        tag.put(key, list);
    }

    private static Vector3f getVec3(CompoundTag tag, String key, Vector3f def) {
        ListTag list = tag.getListOrEmpty(key);
        if (list.size() < 3) return def;
        return new Vector3f(list.getFloatOr(0, def.x), list.getFloatOr(1, def.y), list.getFloatOr(2, def.z));
    }

    private static void putVec2(CompoundTag tag, String key, Vector2f v) {
        ListTag list = new ListTag();
        list.add(net.minecraft.nbt.FloatTag.valueOf(v.x));
        list.add(net.minecraft.nbt.FloatTag.valueOf(v.y));
        tag.put(key, list);
    }

    private static Vector2f getVec2(CompoundTag tag, String key, Vector2f def) {
        ListTag list = tag.getListOrEmpty(key);
        if (list.size() < 2) return def;
        return new Vector2f(list.getFloatOr(0, def.x), list.getFloatOr(1, def.y));
    }

    public Collection<PersistedSceneNode> getRoots() {
        return Collections.unmodifiableList(roots);
    }

    public Collection<PersistedSceneNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public @Nullable PersistedSceneNode getNode(String uuid) {
        return nodes.get(uuid);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    void markDirty() {
        dirty = true;
    }

    public PersistedSceneNode createNode(String typeName, String displayName, Vector3f localPos, Vector2f localRot, @Nullable String parentUuid) {
        String uuid = UUID.randomUUID().toString();
        PersistedSceneNode node = new PersistedSceneNode(this, uuid, displayName, typeName);
        node.setLocalPosition(localPos);
        node.setLocalRotation(localRot);

        nodes.put(uuid, node);

        if (parentUuid != null) {
            PersistedSceneNode parent = nodes.get(parentUuid);
            if (parent != null) {
                parent.addChild(node);
            } else {
                roots.add(node);
            }
        } else {
            roots.add(node);
        }

        markDirty();
        return node;
    }

    public void setParent(String childUuid, @Nullable String newParentUuid) {
        PersistedSceneNode child = nodes.get(childUuid);
        if (child == null) return;

        EngineSceneNode oldParent = child.getParent();
        if (oldParent != null) {
            oldParent.removeChild(child);
        } else {
            roots.remove(child);
        }

        if (newParentUuid != null) {
            PersistedSceneNode newParent = nodes.get(newParentUuid);
            if (newParent != null && newParent != child) {
                // prevent cycles
                if (!isDescendantOf(newParent, childUuid)) {
                    newParent.addChild(child);
                } else {
                    roots.add(child);
                }
            } else {
                roots.add(child);
            }
        } else {
            roots.add(child);
        }

        markDirty();
    }

    private boolean isDescendantOf(PersistedSceneNode maybeChild, String uuid) {
        EngineSceneNode p = maybeChild.getParent();
        while (p != null) {
            if (uuid.equals(p.getUUID())) return true;
            p = p.getParent();
        }
        return false;
    }

    public void removeSubtree(String uuid) {
        PersistedSceneNode node = nodes.get(uuid);
        if (node == null) return;

        // Detach from parent/root first
        EngineSceneNode p = node.getParent();
        if (p != null) {
            p.removeChild(node);
        } else {
            roots.remove(node);
        }

        // Remove children recursively
        for (EngineSceneNode child : new ArrayList<>(node.getChildren())) {
            removeSubtree(child.getUUID());
        }

        node.getChildren().clear();
        nodes.remove(uuid);
        markDirty();
    }

    public CompoundTag toNbt() {
        CompoundTag rootTag = new CompoundTag();
        rootTag.putInt("Version", FORMAT_VERSION);

        ListTag nodeList = new ListTag();
        for (PersistedSceneNode node : nodes.values()) {
            CompoundTag n = new CompoundTag();
            n.putString("Uuid", node.getUUID());
            n.putString("Name", node.getDisplayName());
            n.putString("Type", node.getTypeName());

            EngineSceneNode p = node.getParent();
            if (p != null) {
                n.putString("Parent", p.getUUID());
            }

            putVec3(n, "LocalPos", node.getLocalPosition());
            putVec2(n, "LocalRot", node.getLocalRotation());

            writeProps(n, node.getProperties());

            nodeList.add(n);
        }
        rootTag.put("Nodes", nodeList);
        return rootTag;
    }
}
