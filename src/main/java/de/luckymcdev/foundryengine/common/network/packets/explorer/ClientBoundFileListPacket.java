package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.client.editor.builtin.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Sent by the server in response to {@link ServerBoundRequestFileListPacket}.
 * Carries a flat list of (relativePath, isDirectory) pairs representing the
 * remote directory tree rooted at the requested path.
 */
public record ClientBoundFileListPacket(
        String rootPath,
        List<RemoteEntry> entries
) implements AbstractPacket<ClientBoundFileListPacket> {

    public static final Definition<ClientBoundFileListPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("file_list")),
            PacketBounds.CLIENT,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ClientBoundFileListPacket::rootPath,
                    RemoteEntry.CODEC.apply(ByteBufCodecs.list()), ClientBoundFileListPacket::entries,
                    ClientBoundFileListPacket::new
            ),
            ClientBoundFileListPacket::handleClient,
            null
    );

    @Override
    public Type<ClientBoundFileListPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ClientBoundFileListPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleClient(IPayloadContext ctx) {
        ctx.enqueueWork(() -> FileExplorerPanel.INSTANCE.receiveRemoteFileList(rootPath, entries));
    }

    public record RemoteEntry(String relativePath, boolean isDirectory) {
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoteEntry> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, RemoteEntry::relativePath,
                ByteBufCodecs.BOOL, RemoteEntry::isDirectory,
                RemoteEntry::new
        );
    }
}