package de.luckymcdev.foundryengine.common.network.packets.editor;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GiveItemPacket(String itemId) implements AbstractPacket<GiveItemPacket> {

	public static final Definition<GiveItemPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("give_item")),
		PacketBounds.SERVER,
		StreamCodec.composite(
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), GiveItemPacket::itemId,
			GiveItemPacket::new
		),
		null,
		GiveItemPacket::handleServer
	);

	@Override
	public Type<GiveItemPacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, GiveItemPacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ServerPlayer player = AbstractPacket.serverPlayer(ctx);
		if (player == null || !PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
			return;
		}

		Identifier location = Identifier.tryParse(itemId);
		if (location == null) {
			return;
		}

		BuiltInRegistries.ITEM.getOptional(location).ifPresent(item -> {
			ItemStack stack = new ItemStack(item, 1);
			if (!player.getInventory().add(stack)) {
				player.drop(stack, false);
			}
		});
	}
}
