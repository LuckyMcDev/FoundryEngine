package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.client.editor.panel.explorer.ExplorerPanel;
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

public record ClientBoundExplorerPacket(
	Action action,
	String path,
	String payload,
	List<RemoteEntry> entries,
	List<String> resourceIds
) implements AbstractPacket<ClientBoundExplorerPacket> {

	public static final StreamCodec<RegistryFriendlyByteBuf, RemoteEntry> REMOTE_ENTRY_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, RemoteEntry::relativePath,
		ByteBufCodecs.BOOL, RemoteEntry::isDirectory,
		RemoteEntry::new
	);
	private static final StreamCodec<ByteBuf, Action> ACTION_CODEC = ActionCodec.streamCodec(Action.values(), Action.FILE_LIST);
	public static final Definition<ClientBoundExplorerPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("client_explorer")),
		PacketBounds.CLIENT,
		StreamCodec.composite(
			ACTION_CODEC, ClientBoundExplorerPacket::action,
			ByteBufCodecs.STRING_UTF8, ClientBoundExplorerPacket::path,
			ByteBufCodecs.STRING_UTF8, ClientBoundExplorerPacket::payload,
			REMOTE_ENTRY_CODEC.apply(ByteBufCodecs.list()), ClientBoundExplorerPacket::entries,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), ClientBoundExplorerPacket::resourceIds,
			ClientBoundExplorerPacket::new
		),
		ClientBoundExplorerPacket::handleClient,
		null
	);

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
			switch (action) {
				case FILE_LIST -> ExplorerPanel.INSTANCE.receiveRemoteFileList(entries);
				case FILE_CONTENT -> ExplorerPanel.INSTANCE.receiveRemoteFileContent(path, payload);
				case RESOURCE_LIST -> ExplorerPanel.INSTANCE.receiveResourceList(resourceIds);
				case RESOURCE_CONTENT -> ExplorerPanel.INSTANCE.receiveResourceContent(path, payload);
			}
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
