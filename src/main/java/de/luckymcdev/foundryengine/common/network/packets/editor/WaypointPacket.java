package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.waypoint.Waypoint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WaypointPacket(String action, int x, int y, int z, String icon, String name,
                             int color) implements AbstractPacket<WaypointPacket> {

    public static final Type<WaypointPacket> TYPE = AbstractPacket.createType(Common.id("waypoint_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WaypointPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WaypointPacket::action,
            ByteBufCodecs.INT, WaypointPacket::x,
            ByteBufCodecs.INT, WaypointPacket::y,
            ByteBufCodecs.INT, WaypointPacket::z,
            ByteBufCodecs.STRING_UTF8, WaypointPacket::icon,
            ByteBufCodecs.STRING_UTF8, WaypointPacket::name,
            ByteBufCodecs.INT, WaypointPacket::color,
            WaypointPacket::new
    );

    public static final Definition<WaypointPacket> DEFINITION = new Definition<>(
            TYPE,
            PacketBounds.SERVER,
            CODEC,
            null,
            (packet, ctx) -> handleServer(packet, ctx)
    );

    private static void handleServer(WaypointPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ServerLevel level = player.level();
            var manager = Common.getWaypointManager();

            switch (packet.action()) {
                case "ADD" -> manager.addWaypoint(level,
                        new Waypoint(packet.name(), packet.icon(), packet.x(), packet.y(), packet.z(), packet.color()));
                case "REMOVE" -> manager.removeWaypoint(level, packet.x(), packet.y(), packet.z());
                case "CLEAR" -> manager.clearWaypoints(level);
                default -> {
                    return;
                }
            }

            manager.syncToDimension(level);
        });
    }

    public static WaypointPacket add(int x, int y, int z, String name, String icon, int color) {
        return new WaypointPacket("ADD", x, y, z, icon, name, color);
    }

    public static WaypointPacket remove(int x, int y, int z) {
        return new WaypointPacket("REMOVE", x, y, z, "", "", 0);
    }

    public static WaypointPacket clear() {
        return new WaypointPacket("CLEAR", 0, 0, 0, "", "", 0);
    }

    @Override
    public Type<WaypointPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.SERVER;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WaypointPacket> getCodec() {
        return CODEC;
    }
}
