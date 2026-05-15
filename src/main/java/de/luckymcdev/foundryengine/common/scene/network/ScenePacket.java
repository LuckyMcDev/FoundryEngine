package de.luckymcdev.foundryengine.common.scene.network;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.scene.storage.SceneSavedData;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Bidirectional scene sync packet.
 */
public record ScenePacket(CompoundTag nbt) implements AbstractPacket<ScenePacket> {

    public static final Definition<ScenePacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("scene_graph_nbt")),
            PacketBounds.SERVER,
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, ScenePacket::nbt, ScenePacket::new),
            null,
            ScenePacket::handleServer
    );

    @Override
    public Type<ScenePacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScenePacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

        ServerLevel level = player.level();
        SceneSavedData data = SceneSavedData.get(level);
        data.setData(this.nbt);
        Common.getSavedDataManager().syncToDimension(level);
    }
}

