package de.luckymcdev.foundryengine.common.network.packets.sync;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenEffectPacket(
        String name,
        int introTicks,
        int holdTicks,
        int outroTicks,
        String lerpType
) implements AbstractPacket<ScreenEffectPacket> {

    public static final Definition<ScreenEffectPacket> DEFINITION = new Definition<>(
            AbstractPacket.createType(Common.id("screen_effect")),
            PacketBounds.BOTH,
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ScreenEffectPacket::name,
                    ByteBufCodecs.INT, ScreenEffectPacket::introTicks,
                    ByteBufCodecs.INT, ScreenEffectPacket::holdTicks,
                    ByteBufCodecs.INT, ScreenEffectPacket::outroTicks,
                    ByteBufCodecs.STRING_UTF8, ScreenEffectPacket::lerpType,
                    ScreenEffectPacket::new
            ),
            ScreenEffectPacket::handleClient,
            null
    );

    @Override
    public Type<ScreenEffectPacket> getType() {
        return DEFINITION.type();
    }

    @Override
    public PacketBounds getBoundTo() {
        return DEFINITION.bounds();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ScreenEffectPacket> getCodec() {
        return DEFINITION.codec();
    }

    @Override
    public void handleClient(IPayloadContext ctx) {
        ctx.enqueueWork(() -> Client.getPostEffectManager().startScreenEffect(name(), introTicks(), holdTicks(), outroTicks(), lerpType()));
    }
}
