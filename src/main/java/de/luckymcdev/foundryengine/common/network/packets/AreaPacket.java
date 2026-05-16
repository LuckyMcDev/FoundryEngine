package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.AABBCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AreaPacket(
        String id,
        AABB bounds,
        Identifier dimensionId,
        boolean isRemoval,
        boolean isUpdate
) implements AbstractPacket<AreaPacket> {

    public static final Type<AreaPacket> TYPE = AbstractPacket.createType(Common.id("area_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AreaPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AreaPacket::id,
            AABBCodec.INSTANCE, AreaPacket::bounds,
            Identifier.STREAM_CODEC, AreaPacket::dimensionId,
            ByteBufCodecs.BOOL, AreaPacket::isRemoval,
            ByteBufCodecs.BOOL, AreaPacket::isUpdate,
            AreaPacket::new
    );

    public static final Definition<AreaPacket> DEFINITION = new Definition<AreaPacket>(
            TYPE,
            PacketBounds.SERVER,
            CODEC,
            null,
            (packet, ctx) -> handleServer(packet, ctx)
    );

    private static void handleServer(AreaPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            var level = player.level();
            if (level == null) return;

            var areaManager = Common.getAreaManager();
            var dimensionKey = level.dimension();

            if (packet.isSyncRequest()) {
                // Client requested a sync - send current areas
                areaManager.syncAreasToPlayer(player);
                return;
            }

            if (packet.isRemoval()) {
                // Find and remove the area
                var areas = areaManager.getAreasForDimension(dimensionKey);
                areas.stream()
                        .filter(a -> a.id().equals(packet.id()))
                        .findFirst()
                        .ifPresent(area -> areaManager.remove(level, area));
            } else if (packet.isUpdate()) {
                // Update existing area bounds
                var updatedArea = new Area(packet.id(), packet.bounds(), dimensionKey);
                areaManager.update(level, updatedArea);
            } else {
                // Create and register the area
                var area = new Area(packet.id(), packet.bounds(), dimensionKey);
                areaManager.register(level, area);
            }

            // Sync areas back to the client
            areaManager.syncAreasToPlayer(player);
        });
    }

    public static AreaPacket create(Area area) {
        return new AreaPacket(
                area.id(),
                area.bounds(),
                area.dimension().identifier(),
                false,
                false
        );
    }

    public static AreaPacket update(Area area) {
        return new AreaPacket(
                area.id(),
                area.bounds(),
                area.dimension().identifier(),
                false,
                true
        );
    }

    public static AreaPacket remove(String areaId, Identifier dimensionId) {
        return new AreaPacket(areaId, new AABB(0, 0, 0, 0, 0, 0), dimensionId, true, false);
    }

    public static AreaPacket requestSync(Identifier dimensionId) {
        return new AreaPacket("REQUEST_SYNC", new AABB(0, 0, 0, 0, 0, 0), dimensionId, false, false);
    }

    @Override
    public Type<AreaPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.SERVER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AreaPacket> getCodec() {
        return CODEC;
    }

    public boolean isSyncRequest() {
        return id.equals("REQUEST_SYNC");
    }
}