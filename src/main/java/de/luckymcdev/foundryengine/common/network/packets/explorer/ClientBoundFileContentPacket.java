package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.client.editor.panel.explorer.FileExplorerPanel;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent by the server in response to {@link ServerBoundRequestFileContentPacket}.
 * Carries the UTF-8 text content of a single remote file.
 */
public record ClientBoundFileContentPacket(
        String relativePath,
        String content
) implements AbstractPacket<ClientBoundFileContentPacket> {

    public static final Definition<ClientBoundFileContentPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("file_content")),
            PacketBounds.CLIENT,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ClientBoundFileContentPacket::relativePath,
                    ByteBufCodecs.STRING_UTF8, ClientBoundFileContentPacket::content,
                    ClientBoundFileContentPacket::new
            ),
            ClientBoundFileContentPacket::handleClient,
            null
    );

    @Override
    public Type<ClientBoundFileContentPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ClientBoundFileContentPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleClient(IPayloadContext ctx) {
        ctx.enqueueWork(() -> FileExplorerPanel.INSTANCE.receiveRemoteFileContent(relativePath, content));
    }
}