package de.luckymcdev.foundryengine.common.network.packets.explorer;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sent by the client when the user saves an edited remote file.
 * The server writes the content to disk (within {@link Common#DIRECTORY}).
 */
public record ServerBoundSaveFilePacket(
        String relativePath,
        String content
) implements AbstractPacket<ServerBoundSaveFilePacket> {

    public static final Definition<ServerBoundSaveFilePacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("save_file")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerBoundSaveFilePacket::relativePath,
                    ByteBufCodecs.STRING_UTF8, ServerBoundSaveFilePacket::content,
                    ServerBoundSaveFilePacket::new
            ),
            null,
            ServerBoundSaveFilePacket::handleServer
    );

    @Override
    public Type<ServerBoundSaveFilePacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundSaveFilePacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!PermissionChecks.COMMANDS_OWNER.check(player.permissions())) return;

        Path serverRoot = Common.DIRECTORY;
        Path target = serverRoot.resolve(relativePath).normalize();

        // Jail-break check — never write outside of Common.DIRECTORY
        if (!target.startsWith(serverRoot)) {
            Common.LOGGER.warn("Player {} attempted path traversal on save: {}", player.getName().getString(), relativePath);
            return;
        }

        // Only allow overwriting existing files — no creation of new remote files via this packet
        if (!Files.isRegularFile(target)) {
            Common.LOGGER.warn("Player {} tried to save to non-existent remote file: {}", player.getName().getString(), relativePath);
            return;
        }

        try {
            Files.writeString(target, content);
            Common.LOGGER.info("Player {} saved remote file: {}", player.getName().getString(), relativePath);
        } catch (IOException e) {
            Common.LOGGER.error("Failed to write remote file {} for player {}: {}", relativePath, player.getName().getString(), e.getMessage());
        }
    }
}