package de.luckymcdev.foundryengine.common.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.scene.ScenePanel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SceneManager {
    private final Map<String, EngineSceneNode> nodes = new HashMap<>();
    private final List<EngineSceneNode> rootNodes = new ArrayList<>();

    private SceneZone activeZone;
    private boolean followPlayer = false;

    public void register(EngineSceneNode node) {
        nodes.put(node.getUUID(), node);
        if (node.getParent() == null) {
            rootNodes.add(node);
        }
    }

    public void remove(EngineSceneNode node) {
        nodes.remove(node.getUUID());
        rootNodes.remove(node);
    }

    @Nullable
    public EngineSceneNode getNode(String uuid) {
        return nodes.get(uuid);
    }

    public Collection<EngineSceneNode> getRootNodes() {
        return Collections.unmodifiableList(rootNodes);
    }

    public Collection<EngineSceneNode> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public Collection<EngineSceneNode> getFilteredRoots() {
        List<EngineSceneNode> filtered = new ArrayList<>(rootNodes);
        if (followPlayer) {
            var player = Client.getPlayer();
            if (player != null) {
                ChunkPos chunk = player.chunkPosition();
                filtered.removeIf(node -> !isInChunk(node, chunk));
            }
        } else if (activeZone != null) {
            filtered.removeIf(node -> !activeZone.contains(node.getPosition().x, node.getPosition().z));
        }
        return filtered;
    }

    private boolean isInChunk(EngineSceneNode node, ChunkPos chunk) {
        float x = node.getPosition().x;
        float z = node.getPosition().z;
        return x >= chunk.getMinBlockX() && x <= chunk.getMaxBlockX()
                && z >= chunk.getMinBlockZ() && z <= chunk.getMaxBlockZ();
    }

    public void renderGizmos(@Nullable EngineSceneNode selectedNode) {
        if (!ScenePanel.INSTANCE.showGizmos) return;
        if (selectedNode == null) return;
        selectedNode.drawGizmos();
    }

    public void entityJoinLevel(EntityJoinLevelEvent event) {
        String uuid = event.getEntity().getStringUUID();
        if (nodes.containsKey(uuid)) return;
        EntitySceneNode node = new EntitySceneNode(event.getEntity());
        register(node);
    }

    public void entityLeaveLevel(EntityLeaveLevelEvent event) {
        String uuid = event.getEntity().getStringUUID();
        EngineSceneNode node = nodes.get(uuid);
        if (node != null) {
            remove(node);
        }
    }

    public SceneZone getActiveZone() {
        return activeZone;
    }

    public void setActiveZone(SceneZone zone) {
        this.activeZone = zone;
        this.followPlayer = false;
    }

    public void setFollowPlayer(boolean follow) {
        this.followPlayer = follow;
        if (follow) this.activeZone = null;
    }

    public boolean isFollowingPlayer() {
        return followPlayer;
    }
}