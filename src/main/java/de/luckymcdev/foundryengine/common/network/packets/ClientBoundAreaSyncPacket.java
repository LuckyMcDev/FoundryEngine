package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.AABBCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClientBoundAreaSyncPacket(
        Identifier dimensionId,
        List<AreaData> areas
) implements AbstractPacket<ClientBoundAreaSyncPacket> {

    public static final Type<ClientBoundAreaSyncPacket> TYPE = AbstractPacket.createType(Common.id("area_sync_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundAreaSyncPacket> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ClientBoundAreaSyncPacket::dimensionId,
            AreaData.LIST_CODEC, ClientBoundAreaSyncPacket::areas,
            ClientBoundAreaSyncPacket::new
    );

    public static final Definition<ClientBoundAreaSyncPacket> DEFINITION = new Definition<ClientBoundAreaSyncPacket>(
            TYPE,
            PacketBounds.CLIENT,
            CODEC,
            (packet, ctx) -> handleClient(packet, ctx),
            null
    );

    public static void handleClient(ClientBoundAreaSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var client = Minecraft.getInstance();
            if (client.level == null) return;

            var dimensionKey = ResourceKey.create(Registries.DIMENSION, packet.dimensionId());
            var areaManager = Common.getAreaManager();

            // Clear existing areas for this dimension on client
            var existingAreas = new ArrayList<>(areaManager.getAreasForDimension(dimensionKey));
            existingAreas.forEach(area -> areaManager.remove(null, area));

            // Add new areas
            for (var areaData : packet.areas()) {
                var area = new Area(areaData.id(), areaData.bounds(), dimensionKey);
                areaManager.register(null, area);
            }
        });
    }

    public static ClientBoundAreaSyncPacket create(Identifier dimensionId, List<Area> areas) {
        var areaDataList = new ArrayList<AreaData>();
        for (var area : areas) {
            areaDataList.add(new AreaData(area.id(), area.bounds()));
        }
        return new ClientBoundAreaSyncPacket(dimensionId, areaDataList);
    }

    @Override
    public Type<ClientBoundAreaSyncPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.CLIENT;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ClientBoundAreaSyncPacket> getCodec() {
        return CODEC;
    }

    public record AreaData(String id, AABB bounds) {
        public static final StreamCodec<RegistryFriendlyByteBuf, AreaData> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, AreaData::id,
                AABBCodec.INSTANCE, AreaData::bounds,
                AreaData::new
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, List<AreaData>> LIST_CODEC =
                CODEC.apply(ByteBufCodecs.list());
    }
}