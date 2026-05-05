package de.luckymcdev.foundryengine.common.cutscene.network;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneCommandPacket(String command) implements AbstractPacket<CutsceneCommandPacket> {

    public static final Definition<CutsceneCommandPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("cutscene_command")),
            PacketBounds.SERVER,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, CutsceneCommandPacket::command,
                    CutsceneCommandPacket::new
            ),
            null,
            CutsceneCommandPacket::handleServer
    );

    @Override
    public Type<CutsceneCommandPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CutsceneCommandPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        if (this.command == null || this.command.isBlank()) return;
        if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

        MinecraftServer server = player.level().getServer();
        if (server == null) return;

        CommandSourceStack source = player.createCommandSourceStack()
                .withMaximumPermission(PermissionSet.ALL_PERMISSIONS)
                .withSuppressedOutput();
        server.getCommands().performPrefixedCommand(source, this.command);
    }
}
