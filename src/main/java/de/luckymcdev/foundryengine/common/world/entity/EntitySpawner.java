package de.luckymcdev.foundryengine.common.world.entity;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public final class EntitySpawner {
    private EntitySpawner() {
    }

    public static <T extends Entity> T spawnServer(
            net.minecraft.server.level.ServerLevel level,
            EntityType<T> entityType,
            Vec3 pos
    ) {
        return spawnServer(level, entityType, pos, 0f, 0f, null);
    }

    public static <T extends Entity> T spawnServer(
            net.minecraft.server.level.ServerLevel level,
            EntityType<T> entityType,
            Vec3 pos,
            Consumer<T> configurator
    ) {
        return spawnServer(level, entityType, pos, 0f, 0f, configurator);
    }

    public static <T extends Entity> T spawnServer(
            net.minecraft.server.level.ServerLevel level,
            EntityType<T> entityType,
            Vec3 pos,
            float yRot,
            float xRot,
            Consumer<T> configurator
    ) {
        T entity = entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (entity == null) {
            Common.LOGGER.warn("EntitySpawner: EntityType '{}' returned null on create",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            return null;
        }
        entity.snapTo(pos.x, pos.y, pos.z, yRot, xRot);
        if (configurator != null) {
            configurator.accept(entity);
        }
        level.addFreshEntity(entity);
        return entity;
    }

}