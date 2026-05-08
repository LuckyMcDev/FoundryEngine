package de.luckymcdev.foundryengine.client.scene;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.scene.EngineSceneNode;
import de.luckymcdev.foundryengine.common.scene.SceneGraph;
import de.luckymcdev.foundryengine.common.scene.WorldEntitySceneNode;
import de.luckymcdev.foundryengine.common.scene.network.ScenePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Client-side scene sync helper.
 */
public final class ClientSceneSync {
    private static boolean requestedSync = false;

    private ClientSceneSync() {
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            requestedSync = false;
            Common.getSceneManager().setClientGraph(new SceneGraph());
            return;
        }

        if (!requestedSync) {
            requestedSync = true;
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("Request", true);
            ClientPacketDistributor.sendToServer(new ScenePacket(tag));
        }
    }

    public static void handlePacket(ScenePacket packet) {
        SceneGraph graph = SceneGraph.fromNbt(packet.nbt());
        Common.getSceneManager().setClientGraph(graph);
    }

    public static void pushToServer(SceneGraph graph) {
        if (graph == null) graph = new SceneGraph();
        ClientPacketDistributor.sendToServer(new ScenePacket(graph.toNbt()));
        graph.clearDirty();
    }

    public static void requestResync() {
        requestedSync = false;
    }

    public static @Nullable EngineSceneNode findNode(@Nullable String uuid) {
        if (uuid == null || uuid.isBlank()) return null;

        var persisted = Common.getSceneManager().getClientGraph().getNode(uuid);
        if (persisted != null) return persisted;

        var level = Minecraft.getInstance().level;
        if (level == null) return null;

        var entity = level.getEntity(java.util.UUID.fromString(uuid));
        if (entity != null) return new WorldEntitySceneNode(entity);

        return null;
    }
}
