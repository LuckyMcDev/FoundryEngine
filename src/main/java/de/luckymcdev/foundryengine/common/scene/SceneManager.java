package de.luckymcdev.foundryengine.common.scene;

import org.jspecify.annotations.Nullable;

/**
 * Common entry point for the scene system.
 *
 * <p>Server-side, the source of truth is {@code SceneSavedData} (per dimension).
 * Client-side, the editor operates on a locally cached {@link SceneGraph} synced from the server.</p>
 */
public final class SceneManager {
    private volatile SceneGraph clientGraph = new SceneGraph();

    public SceneGraph getClientGraph() {
        return clientGraph;
    }

    /**
     * Replace the client-side cached graph (typically from a sync packet).
     */
    public void setClientGraph(SceneGraph graph) {
        this.clientGraph = (graph != null) ? graph : new SceneGraph();
    }

    public @Nullable PersistedSceneNode getPersistedNode(String uuid) {
        return clientGraph.getNode(uuid);
    }
}

