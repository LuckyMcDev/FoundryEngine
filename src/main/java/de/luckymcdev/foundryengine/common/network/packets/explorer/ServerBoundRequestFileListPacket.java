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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sent by the client to ask the server for a recursive listing of its
 * {@link Common#DIRECTORY} tree. The server responds with {@link ClientBoundFileListPacket}.
 */
public record ServerBoundRequestFileListPacket(
        String rootPath
) implements AbstractPacket<ServerBoundRequestFileListPacket> {

    public static final Definition<ServerBoundRequestFileListPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("request_file_list")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerBoundRequestFileListPacket::rootPath,
                    ServerBoundRequestFileListPacket::new
            ),
            null,
            ServerBoundRequestFileListPacket::handleServer
    );

    @Override
    public Type<ServerBoundRequestFileListPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundRequestFileListPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (!PermissionChecks.COMMANDS_OWNER.check(player.permissions())) return;

        Path serverRoot = Common.DIRECTORY;

        // Resolve and jail-break check — only allow paths inside Common.DIRECTORY
        Path resolved = serverRoot.resolve(rootPath).normalize();
        if (!resolved.startsWith(serverRoot)) {
            Common.LOGGER.warn("Player {} attempted path traversal: {}", player.getName().getString(), rootPath);
            return;
        }

        File rootFile = resolved.toFile();
        if (!rootFile.exists() || !rootFile.isDirectory()) return;

        List<ClientBoundFileListPacket.RemoteEntry> entries = new ArrayList<>();
        collectEntries(serverRoot, rootFile, entries);

        PacketDistributor.sendToPlayer(player, new ClientBoundFileListPacket(rootPath, entries));
    }

    private void collectEntries(Path serverRoot, File dir, List<ClientBoundFileListPacket.RemoteEntry> out) {
        File[] children = dir.listFiles();
        if (children == null) return;

        for (File child : children) {
            String relative = serverRoot.relativize(child.toPath().normalize()).toString().replace('\\', '/');
            out.add(new ClientBoundFileListPacket.RemoteEntry(relative, child.isDirectory()));
            if (child.isDirectory()) {
                collectEntries(serverRoot, child, out);
            }
        }
    }
}