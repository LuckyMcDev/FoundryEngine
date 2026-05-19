package de.luckymcdev.foundryengine.common.network.packets.world;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3fc;

public record ServerBoundTeleportPacket(Vector3fc position) implements AbstractPacket<ServerBoundTeleportPacket> {

    public static final Definition<ServerBoundTeleportPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("teleport_to_v3f")),
            PacketBounds.SERVER,
            StreamCodec.composite(ByteBufCodecs.VECTOR3F, ServerBoundTeleportPacket::position, ServerBoundTeleportPacket::new),
            null,
            ServerBoundTeleportPacket::handleServer
    );

    @Override
    public Type<ServerBoundTeleportPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundTeleportPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;
            player.teleportTo(position.x(), position.y(), position.z());
        }
    }
}
