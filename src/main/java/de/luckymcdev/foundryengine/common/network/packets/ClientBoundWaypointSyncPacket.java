package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.client.waypoint.Waypoint;
import de.luckymcdev.foundryengine.client.waypoint.WaypointManager;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientBoundWaypointSyncPacket(CompoundTag data) implements AbstractPacket<ClientBoundWaypointSyncPacket> {

    public static final Type<ClientBoundWaypointSyncPacket> TYPE = AbstractPacket.createType(Common.id("waypoint_sync_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundWaypointSyncPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, ClientBoundWaypointSyncPacket::data,
            ClientBoundWaypointSyncPacket::new
    );

    public static final Definition<ClientBoundWaypointSyncPacket> DEFINITION = new Definition<>(
            TYPE,
            PacketBounds.CLIENT,
            CODEC,
            (packet, ctx) -> handleClient(packet, ctx),
            null
    );

    private static void handleClient(ClientBoundWaypointSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            WaypointManager.clearWaypoints();
            ListTag list = packet.data().getListOrEmpty("Waypoints");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompoundOrEmpty(i);
                int x = tag.getInt("x").orElse(0);
                int y = tag.getInt("y").orElse(0);
                int z = tag.getInt("z").orElse(0);
                String name = tag.getString("name").orElse("");
                String iconStr = tag.getString("icon").orElse("I");
                int colorInt = tag.getInt("color").orElse(0xFFFFFFFF);

                Component icon = Component.literal(iconStr).setStyle(ChatIcons.ICONS);
                Color color = new Color(colorInt);
                WaypointManager.addWaypoint(new Waypoint(icon, name, new Vec3i(x, y, z), color));
            }
        });
    }

    @Override
    public Type<ClientBoundWaypointSyncPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.CLIENT;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ClientBoundWaypointSyncPacket> getCodec() {
        return CODEC;
    }
}
