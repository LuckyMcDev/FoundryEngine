package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SavedDataSyncPacket(CompoundTag data) implements AbstractPacket<SavedDataSyncPacket> {

    public static final Type<SavedDataSyncPacket> TYPE = AbstractPacket.createType(Common.id("savedata_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SavedDataSyncPacket::data,
            SavedDataSyncPacket::new
    );

    public static final Definition<SavedDataSyncPacket> DEFINITION = new Definition<>(
            TYPE,
            PacketBounds.CLIENT,
            CODEC,
            (packet, ctx) -> handleClient(packet, ctx),
            null
    );

    private static void handleClient(SavedDataSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Common.getSavedDataManager().dispatchSync(packet.data()));
    }

    @Override
    public Type<SavedDataSyncPacket> getType() {
        return TYPE;
    }

    @Override
    public PacketBounds getBoundTo() {
        return PacketBounds.CLIENT;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SavedDataSyncPacket> getCodec() {
        return CODEC;
    }
}
