package de.luckymcdev.foundryengine.common.network.packets.world;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record ServerBoundSpawnEntityPacket(
        String entityTypeId,
        double x,
        double y,
        double z,
        float yRot,
        float xRot
) implements AbstractPacket<ServerBoundSpawnEntityPacket> {

    public static final Definition<ServerBoundSpawnEntityPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("spawn_entity")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerBoundSpawnEntityPacket::entityTypeId,
                    ByteBufCodecs.DOUBLE, ServerBoundSpawnEntityPacket::x,
                    ByteBufCodecs.DOUBLE, ServerBoundSpawnEntityPacket::y,
                    ByteBufCodecs.DOUBLE, ServerBoundSpawnEntityPacket::z,
                    ByteBufCodecs.FLOAT, ServerBoundSpawnEntityPacket::yRot,
                    ByteBufCodecs.FLOAT, ServerBoundSpawnEntityPacket::xRot,
                    ServerBoundSpawnEntityPacket::new
            ),
            null,
            ServerBoundSpawnEntityPacket::handleServer
    );

    @Override
    public Type<ServerBoundSpawnEntityPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundSpawnEntityPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

        ServerLevel level = serverPlayer.level();

        Identifier location = Identifier.tryParse(entityTypeId);
        if (location == null) {
            Common.LOGGER.warn("ServerBoundSpawnEntityPacket: invalid entity type id '{}'", entityTypeId);
            return;
        }

        Optional<EntityType<?>> maybeType = BuiltInRegistries.ENTITY_TYPE.getOptional(location);
        if (maybeType.isEmpty()) {
            Common.LOGGER.warn("ServerBoundSpawnEntityPacket: unknown entity type '{}'", entityTypeId);
            return;
        }

        EntityType<?> entityType = maybeType.get();
        Entity entity = entityType.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) {
            Common.LOGGER.warn("ServerBoundSpawnEntityPacket: entity type '{}' returned null on create", entityTypeId);
            return;
        }

        entity.snapTo(x, y, z, yRot, xRot);
        level.addFreshEntity(entity);

        Common.LOGGER.debug("Spawned '{}' at ({}, {}, {}) in {}", entityTypeId, x, y, z, level.dimension().identifier());
    }
}
