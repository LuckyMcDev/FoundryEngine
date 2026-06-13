package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.FolderHash;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Packet sent from Client to Server to verify bundle directory synchronization.
 */
public record BundleHashPacket(String clientHash) implements AbstractPacket<BundleHashPacket> {

    public static final Definition<BundleHashPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("bundle_hash_packet")),
            PacketBounds.SERVER,
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, BundleHashPacket::clientHash, BundleHashPacket::new),
            null,
            BundleHashPacket::handleServer
    );

    @Override
    public Type<BundleHashPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BundleHashPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            try {
                String serverHash = FolderHash.hashFolder(Common.BUNDLES);
                if (!Objects.equals(clientHash, serverHash)) {
                    if (ctx.player() instanceof ServerPlayer player) {
                        player.sendSystemMessage(Component.literal("§c Bundle mismatch! §7Your local bundles do not match the server's files. Please sync your 'bundles' folder."));
                        Common.LOGGER.warn("Bundle hash mismatch for player {}: Client={} Server={}",
                                player.getScoreboardName(), clientHash, serverHash);
                    }
                } else {
                    Common.LOGGER.info("Bundle hash verified on server");
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                Common.LOGGER.error("Failed to verify bundle hash on server", e);
            }
        });
    }
}