package de.luckymcdev.foundryengine.common.cutscene.network;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.storage.CutsceneSavedData;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutscenePacket(CompoundTag nbt) implements AbstractPacket<CutscenePacket> {

    public static final Definition<CutscenePacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("cutscene_nbt")),
            PacketBounds.BOTH,
            StreamCodec.composite(ByteBufCodecs.COMPOUND_TAG, CutscenePacket::nbt, CutscenePacket::new),
            CutscenePacket::handleClient,
            CutscenePacket::handleServer
    );

    @Override
    public Type<CutscenePacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CutscenePacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleClient(IPayloadContext ctx) {
        ctx.enqueueWork(() -> de.luckymcdev.foundryengine.client.cutscene.ClientCutsceneManager.handlePacket(this));
    }

    @Override
    public void handleServer(IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        if (!PermissionChecks.COMMANDS_GAMEMASTER.check(player.permissions())) return;

        ServerLevel level = player.level();
        CutsceneSavedData data = CutsceneSavedData.get(level);
        data.setData(this.nbt);
        Common.getSavedDataManager().syncToDimension(level);
    }
}
