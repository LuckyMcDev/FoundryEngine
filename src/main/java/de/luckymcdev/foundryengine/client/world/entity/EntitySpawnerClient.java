package de.luckymcdev.foundryengine.client.world.entity;

import de.luckymcdev.foundryengine.common.network.packets.world.ServerBoundSpawnEntityPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class EntitySpawnerClient {
    private EntitySpawnerClient() {
    }

    public static void spawnFromClient(String entityTypeId, Vec3 pos) {
        spawnFromClient(entityTypeId, pos, 0f, 0f);
    }

    public static void spawnFromClient(String entityTypeId, Vec3 pos, float yRot, float xRot) {
        ClientPacketDistributor.sendToServer(
                new ServerBoundSpawnEntityPacket(entityTypeId, pos.x, pos.y, pos.z, yRot, xRot)
        );
    }

    public static <T extends Entity> void spawnFromClient(EntityType<T> entityType, Vec3 pos) {
        spawnFromClient(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString(), pos);
    }
}
