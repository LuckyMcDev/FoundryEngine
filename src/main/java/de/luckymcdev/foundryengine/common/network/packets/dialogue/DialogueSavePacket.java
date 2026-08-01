package de.luckymcdev.foundryengine.common.network.packets.dialogue;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
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
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) {
				return;
			}
			var mgr = Common.getDialogueManager();
			mgr.applyNbt(data);
			mgr.save();
			mgr.syncToAll();
		});
	}
}
