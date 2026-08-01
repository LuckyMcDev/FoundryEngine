package de.luckymcdev.foundryengine.common.network.packets.sync;

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
		PacketBounds.CLIENT,
		StreamCodec.composite(
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), ScreenEffectPacket::name,
			ByteBufCodecs.INT, ScreenEffectPacket::introTicks,
			ByteBufCodecs.INT, ScreenEffectPacket::holdTicks,
			ByteBufCodecs.INT, ScreenEffectPacket::outroTicks,
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), ScreenEffectPacket::lerpType,
			ScreenEffectPacket::new
		),
		ScreenEffectPacket::handleClient,
		null
	);
	public static volatile java.util.function.Consumer<ScreenEffectPacket> CLIENT_HANDLER;
	private static boolean clientHandlerWarned;

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
		ctx.enqueueWork(() -> {
			var handler = CLIENT_HANDLER;
			if (handler == null) {
				if (!clientHandlerWarned) {
					clientHandlerWarned = true;
					Common.LOGGER.warn("ScreenEffectPacket: client handler not initialized; dropping packet");
				}
				return;
			}
			handler.accept(this);
		});
	}
}
