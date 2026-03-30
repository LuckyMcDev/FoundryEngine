package de.luckymcdev.foundryengine.common.network;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TestPacket(float value) implements AbstractPacket<TestPacket> {

    public static final Definition<TestPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("test_packet")),
            PacketBounds.SERVER,
            StreamCodec.composite(ByteBufCodecs.FLOAT, TestPacket::value, TestPacket::new),
            null,
            TestPacket::handleServer
    );

    @Override
    public Type<TestPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TestPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        var player = ctx.player();
        if (player instanceof ServerPlayer serverPlayer) {
            Common.LOGGER.info("Received TestPacket on server with server player and value: {}", value);
            serverPlayer.hurtServer(serverPlayer.level(), serverPlayer.damageSources().cactus(), value);
        }
        Common.LOGGER.info("Received TestPacket on server with value: {}", value);
    }
}