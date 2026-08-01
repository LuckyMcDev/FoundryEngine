package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.ActionCodec;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.waypoint.Waypoint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WaypointPacket(
	Action action,
	int x,
	int y,
	int z,
	String icon,
	String name,
	Color color
) implements AbstractPacket<WaypointPacket> {

	public static final Type<WaypointPacket> TYPE = AbstractPacket.createType(Common.id("waypoint_packet"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WaypointPacket> CODEC = StreamCodec.composite(
		ActionCodec.streamCodec(Action.values(), Action.ADD), WaypointPacket::action,
		ByteBufCodecs.INT, WaypointPacket::x,
		ByteBufCodecs.INT, WaypointPacket::y,
		ByteBufCodecs.INT, WaypointPacket::z,
		ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), WaypointPacket::icon,
		ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), WaypointPacket::name,
		ByteBufCodecs.INT.map(Color::new, Color::argb), WaypointPacket::color,
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
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
				return;
			}
			ServerLevel level = player.level();
			var manager = Common.getWaypointManager();

			switch (packet.action()) {
				case ADD -> manager.addWaypoint(level,
					new Waypoint(packet.name(), packet.icon(), packet.x(), packet.y(), packet.z(), packet.color()));
				case REMOVE -> manager.removeWaypoint(level, packet.x(), packet.y(), packet.z());
				case CLEAR -> manager.clearWaypoints(level);
			}

			manager.syncToAll();
		});
	}

	public static WaypointPacket add(int x, int y, int z, String name, String icon, Color color) {
		return new WaypointPacket(Action.ADD, x, y, z, icon, name, color);
	}

	public static WaypointPacket remove(int x, int y, int z) {
		return new WaypointPacket(Action.REMOVE, x, y, z, "", "", new Color(0));
	}

	public static WaypointPacket clear() {
		return new WaypointPacket(Action.CLEAR, 0, 0, 0, "", "", new Color(0));
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

	public enum Action {
		ADD, REMOVE, CLEAR
	}
}
