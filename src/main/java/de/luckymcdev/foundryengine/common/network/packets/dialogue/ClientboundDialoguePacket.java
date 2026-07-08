package de.luckymcdev.foundryengine.common.network.packets.dialogue;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.dialogue.DialogueNode;
import de.luckymcdev.foundryengine.common.dialogue.DialogueSession;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.ActionCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client packet for dialogue lifecycle.
 * Carries serialized {@link DialogueSession} and {@link DialogueNode} to the
 * client's {@link de.luckymcdev.foundryengine.client.dialogue.ClientDialogueManager}.
 */
public record ClientboundDialoguePacket(
	Action action,
	Identifier treeId,
	CompoundTag session,
	CompoundTag node
) implements AbstractPacket<ClientboundDialoguePacket> {

	public static final Type<ClientboundDialoguePacket> TYPE = AbstractPacket.createType(Common.id("dialogue_client"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDialoguePacket> CODEC = StreamCodec.composite(
		ActionCodec.streamCodec(Action.values(), Action.SHOW), ClientboundDialoguePacket::action,
		Identifier.STREAM_CODEC, ClientboundDialoguePacket::treeId,
		ByteBufCodecs.COMPOUND_TAG, ClientboundDialoguePacket::session,
		ByteBufCodecs.COMPOUND_TAG, ClientboundDialoguePacket::node,
		ClientboundDialoguePacket::new
	);
	public static final Definition<ClientboundDialoguePacket> DEFINITION = new Definition<>(
		TYPE,
		PacketBounds.CLIENT,
		CODEC,
		(packet, ctx) -> handleClient(packet, ctx),
		null
	);

	public static ClientboundDialoguePacket show(Identifier treeId, DialogueSession session, DialogueNode node) {
		return new ClientboundDialoguePacket(Action.SHOW, treeId, session.toNbt(), node.toNbt());
	}

	public static ClientboundDialoguePacket advance(Identifier treeId, DialogueSession session, DialogueNode node) {
		return new ClientboundDialoguePacket(Action.ADVANCE, treeId, session.toNbt(), node.toNbt());
	}

	public static ClientboundDialoguePacket ended(Identifier treeId) {
		return new ClientboundDialoguePacket(Action.ENDED, treeId, new CompoundTag(), new CompoundTag());
	}

	private static void handleClient(ClientboundDialoguePacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var clientManager = Client.getDialogueManager();
			switch (packet.action()) {
				case SHOW -> {
					var node = DialogueNode.fromNbt(packet.node());
					var session = DialogueSession.fromNbt(packet.session());
					clientManager.startDialogue(packet.treeId(), session, node);
				}
				case ADVANCE -> {
					var node = DialogueNode.fromNbt(packet.node());
					var session = DialogueSession.fromNbt(packet.session());
					clientManager.advanceDialogue(session, node);
				}
				case ENDED -> {
					clientManager.endDialogue();
				}
			}
		});
	}

	@Override
	public Type<ClientboundDialoguePacket> getType() {
		return TYPE;
	}

	@Override
	public PacketBounds getBoundTo() {
		return PacketBounds.CLIENT;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ClientboundDialoguePacket> getCodec() {
		return CODEC;
	}

	public enum Action {
		SHOW, ADVANCE, ENDED
	}
}
