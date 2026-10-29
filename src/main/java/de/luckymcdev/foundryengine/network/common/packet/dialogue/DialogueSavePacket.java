package de.luckymcdev.foundryengine.network.common.packet.dialogue;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.network.common.AbstractPacket;
import de.luckymcdev.foundryengine.network.common.PacketBounds;
import de.luckymcdev.foundryengine.util.common.PermissionChecks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-bound packet to load a full dialogue tree state from the ImGui editor.
 * Requires gamemaster permissions.
 */
public record DialogueSavePacket(CompoundTag data) implements AbstractPacket<DialogueSavePacket> {

	public static final Definition<DialogueSavePacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("dialogue_save")),
		PacketBounds.SERVER,
		StreamCodec.composite(AbstractPacket.GENEROUS_NBT_CODEC, DialogueSavePacket::data, DialogueSavePacket::new),
		null,
		DialogueSavePacket::handleServer
	);

	@Override
	public Type<DialogueSavePacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, DialogueSavePacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer player = AbstractPacket.serverPlayer(ctx);
			if (player == null || !PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
				return;
			}
			var mgr = Common.getDialogueManager();
			mgr.applyNbt(data);
			mgr.save();
			mgr.syncToAll();
		});
	}
}
