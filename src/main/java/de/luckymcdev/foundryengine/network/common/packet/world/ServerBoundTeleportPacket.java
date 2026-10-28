package de.luckymcdev.foundryengine.network.common.packet.world;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.network.common.AbstractPacket;
import de.luckymcdev.foundryengine.network.common.PacketBounds;
import de.luckymcdev.foundryengine.util.common.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3fc;

public record ServerBoundTeleportPacket(Vector3fc position) implements AbstractPacket<ServerBoundTeleportPacket> {

	public static final Definition<ServerBoundTeleportPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("teleport_to_v3f")),
		PacketBounds.SERVER,
		StreamCodec.composite(ByteBufCodecs.VECTOR3F, ServerBoundTeleportPacket::position, ServerBoundTeleportPacket::new),
		null,
		ServerBoundTeleportPacket::handleServer
	);

	@Override
	public Type<ServerBoundTeleportPacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ServerBoundTeleportPacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ServerPlayer player = AbstractPacket.serverPlayer(ctx);
		if (player == null || !PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
			return;
		}
		player.teleportTo(position.x(), position.y(), position.z());
	}
}
