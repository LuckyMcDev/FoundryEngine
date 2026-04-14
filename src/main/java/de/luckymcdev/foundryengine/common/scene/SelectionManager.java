package de.luckymcdev.foundryengine.client.selection;

import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;

public class SelectionManager {
    private static EngineSceneNode selectedNode;

    @Nullable
    public static EngineSceneNode getSelected() {
        return selectedNode;
    }

    public static void setSelected(@Nullable EngineSceneNode node) {
        if (selectedNode != node) {
            selectedNode = node;
            NeoForge.EVENT_BUS.post(new SelectionChangedEvent(selectedNode));
        }
    }

    public static class SelectionChangedEvent extends Event {
        private final EngineSceneNode node;

        public SelectionChangedEvent(EngineSceneNode node) {
            this.node = node;
        }

        public EngineSceneNode getNode() {
            return node;
        }
    }
}