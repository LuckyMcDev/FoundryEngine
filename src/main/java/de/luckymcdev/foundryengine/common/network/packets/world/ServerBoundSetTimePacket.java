package de.luckymcdev.foundryengine.common.network.packets.world;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundSetTimePacket(int timeValue) implements AbstractPacket<ServerBoundSetTimePacket> {

	public static final Definition<ServerBoundSetTimePacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("set_time")),
		PacketBounds.SERVER,
		StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerBoundSetTimePacket::timeValue, ServerBoundSetTimePacket::new),
		null,
		ServerBoundSetTimePacket::handleServer
	);

	@Override
	public Type<ServerBoundSetTimePacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ServerBoundSetTimePacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		if (ctx.player() instanceof ServerPlayer player) {

			if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
				return;
			}

			player.level().dimensionTypeRegistration().value().defaultClock().ifPresent(clock -> {
				player.level().clockManager().setTotalTicks(clock, timeValue);
				Common.LOGGER.info("Player {} set time to {}", player.getName().getString(), timeValue);
			});
		}
	}
}
