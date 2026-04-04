package de.luckymcdev.foundryengine.common.scene;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Collection;

public class SceneManager {
    private final GenericRegistry<String, EngineSceneNode> nodes = new GenericRegistry<>();
    private SceneZone activeZone;
    private boolean followPlayer = false;

    public void register(EngineSceneNode node) {
        this.nodes.register(node.getUUID(), node);
    }

    public void remove(EngineSceneNode node) {
        this.nodes.remove(node.getUUID());
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

    public EngineSceneNode getNode(String uuid) {
        return this.nodes.get(uuid);
    }

    public Collection<EngineSceneNode> getNodes() {
        return this.nodes.values();
    }

    public Collection<EngineSceneNode> getFilteredNodes() {
        if (followPlayer) {
            var player = Client.getPlayer();
            if (player != null) {
                var chunk = player.chunkPosition();
                return getNodes().stream()
                        .filter(node -> {
                            float x = node.getPosition().x;
                            float z = node.getPosition().z;
                            return x >= chunk.getMinBlockX() && x <= chunk.getMaxBlockX()
                                    && z >= chunk.getMinBlockZ() && z <= chunk.getMaxBlockZ();
                        }).toList();
            }
        }

        if (activeZone == null) return getNodes();
        return getNodes().stream()
                .filter(node -> activeZone.contains(node.getPosition().x, node.getPosition().z))
                .toList();
    }

    public void entityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof EngineSceneNode node) {
            this.nodes.register(node.getUUID(), node);
        }
    }

    public void entityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof EngineSceneNode node) {
            this.nodes.remove(node.getUUID());
        }
    }
}