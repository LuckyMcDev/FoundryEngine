package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.network.codecs.ActionCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.function.Consumer;

public record ClientBoundExplorerPacket(
	Action action,
	String path,
	String payload,
	List<RemoteEntry> entries,
	List<String> resourceIds
) implements AbstractPacket<ClientBoundExplorerPacket> {

	public static final StreamCodec<RegistryFriendlyByteBuf, RemoteEntry> REMOTE_ENTRY_CODEC = StreamCodec.composite(
		ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), RemoteEntry::relativePath,
		ByteBufCodecs.BOOL, RemoteEntry::isDirectory,
		RemoteEntry::new
	);
	private static final StreamCodec<ByteBuf, Action> ACTION_CODEC = ActionCodec.streamCodec(Action.values(), Action.FILE_LIST);
	public static final Definition<ClientBoundExplorerPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("client_explorer")),
		PacketBounds.CLIENT,
		StreamCodec.composite(
			ACTION_CODEC, ClientBoundExplorerPacket::action,
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), ClientBoundExplorerPacket::path,
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_FILE_CONTENT_LENGTH), ClientBoundExplorerPacket::payload,
			REMOTE_ENTRY_CODEC.apply(ByteBufCodecs.list(AbstractPacket.MAX_LIST_ELEMENT_COUNT)), ClientBoundExplorerPacket::entries,
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH).apply(ByteBufCodecs.list(AbstractPacket.MAX_LIST_ELEMENT_COUNT)), ClientBoundExplorerPacket::resourceIds,
			ClientBoundExplorerPacket::new
		),
		ClientBoundExplorerPacket::handleClient,
		null
	);
	public static volatile Consumer<ClientBoundExplorerPacket> CLIENT_HANDLER;
	private static boolean clientHandlerWarned;

	@Override
	public Type<ClientBoundExplorerPacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ClientBoundExplorerPacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleClient(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var handler = CLIENT_HANDLER;
			if (handler == null) {
				if (!clientHandlerWarned) {
					clientHandlerWarned = true;
					Common.LOGGER.warn("ClientBoundExplorerPacket: client handler not initialized; dropping packet");
				}
				return;
			}
			handler.accept(this);
		});
	}

	public enum Action {
		FILE_LIST,
		FILE_CONTENT,
		RESOURCE_LIST,
		RESOURCE_CONTENT
	}

	public record RemoteEntry(String relativePath, boolean isDirectory) {
	}
}
