package de.luckymcdev.foundryengine.common.network.packets.world;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerBoundChangeWeatherPacket(
        String weatherType) implements AbstractPacket<ServerBoundChangeWeatherPacket> {

    public static final Definition<ServerBoundChangeWeatherPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("change_weather")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ServerBoundChangeWeatherPacket::weatherType,
                    ServerBoundChangeWeatherPacket::new
            ),
            null,
            ServerBoundChangeWeatherPacket::handleServer
    );

    @Override
    public Type<ServerBoundChangeWeatherPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ServerBoundChangeWeatherPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

            MinecraftServer server = player.level().getServer();
            int duration = 6000;

            switch (weatherType) {
                case "clear" -> server.setWeatherParameters(0, duration, false, false);
                case "rain" -> server.setWeatherParameters(0, duration, true, false);
                case "thunder" -> server.setWeatherParameters(0, duration, true, true);
            }
        }
    }
}
