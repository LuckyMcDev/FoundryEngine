package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sent by the client to open a specific remote file for viewing/editing.
 * The server reads the file and responds with {@link ClientBoundFileContentPacket}.
 * Only text files are supported; binary files are rejected server-side.
 */
public record ServerBoundRequestFileContentPacket(
        String relativePath
) implements AbstractPacket<ServerBoundRequestFileContentPacket> {

    public static final Definition<ServerBoundRequestFileContentPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("request_file_content")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerBoundRequestFileContentPacket::relativePath,
                    ServerBoundRequestFileContentPacket::new
            ),
            null,
            ServerBoundRequestFileContentPacket::handleServer
    );

    @Override
    public Type<ServerBoundRequestFileContentPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundRequestFileContentPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!PermissionChecks.COMMANDS_OWNER.check(player.permissions())) return;

        Path serverRoot = Common.DIRECTORY;
        Path target = serverRoot.resolve(relativePath).normalize();

        // Jail-break check
        if (!target.startsWith(serverRoot)) {
            Common.LOGGER.warn("Player {} attempted path traversal on content read: {}", player.getName().getString(), relativePath);
            return;
        }

        if (!Files.isRegularFile(target)) return;

        try {
            String content = Files.readString(target);
            PacketDistributor.sendToPlayer(player, new ClientBoundFileContentPacket(relativePath, content));
        } catch (IOException e) {
            Common.LOGGER.error("Failed to read remote file {} for player {}: {}", relativePath, player.getName().getString(), e.getMessage());
        }
    }
}