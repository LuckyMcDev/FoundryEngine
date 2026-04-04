package de.luckymcdev.foundryengine.common.scene;

import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.Collection;

public class SceneManager {
    private final GenericRegistry<String, EngineSceneNode> nodes = new GenericRegistry<>();

    public void register(EngineSceneNode node) {
        this.nodes.register(node.getUUID(), node);
    }

    public void remove(EngineSceneNode node) {
        this.nodes.remove(node.getUUID());
    }

    public EngineSceneNode getNode(String uuid) {
        return this.nodes.get(uuid);
    }

    public Collection<EngineSceneNode> getNodes() {
        return this.nodes.values();
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