package de.luckymcdev.foundryengine.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public interface AbstractPacket<T extends AbstractPacket<T>> extends CustomPacketPayload {

    static <T extends CustomPacketPayload> Type<T> createType(Identifier id) {
        return new Type<>(id);
    }

    @Override
    default Type<? extends CustomPacketPayload> type() {
        return getType();
    }

    Type<T> getType();

    PacketBounds getBoundTo();

    StreamCodec<RegistryFriendlyByteBuf, T> getCodec();

    default void handleClient(IPayloadContext ctx) {
    }

    default void handleServer(IPayloadContext ctx) {
    }

    /**
     * A blueprint for registration so we don't need an instance of the packet.
     */
    record Definition<T extends AbstractPacket<T>>(
            Type<T> type,
            PacketBounds bounds,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            @Nullable BiConsumer<T, IPayloadContext> clientHandler,
            @Nullable BiConsumer<T, IPayloadContext> serverHandler
    ) {
    }
}