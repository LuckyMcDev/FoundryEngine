package de.luckymcdev.foundryengine.common.network.packets;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.data.CustomDataReceivedEvent;
import de.luckymcdev.foundryengine.common.network.AbstractPacket;
import de.luckymcdev.foundryengine.common.network.PacketBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.BiConsumer;

public record CustomDataPacket(String id, CompoundTag data) implements AbstractPacket<CustomDataPacket> {

	public static final Definition<CustomDataPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("custom_data")),
		PacketBounds.BOTH,
		StreamCodec.composite(
			ByteBufCodecs.stringUtf8(AbstractPacket.MAX_STRING_LENGTH), CustomDataPacket::id,
			AbstractPacket.GENEROUS_NBT_CODEC, CustomDataPacket::data,
			CustomDataPacket::new
		),
		CustomDataPacket::handleClient,
		CustomDataPacket::handleServer
	);
	public static volatile BiConsumer<CustomDataPacket, IPayloadContext> CLIENT_HANDLER = (pkt, ctx) -> {
	};
	public static volatile BiConsumer<CustomDataPacket, IPayloadContext> SERVER_HANDLER = (pkt, ctx) -> {
	};
	private static boolean clientHandlerWarned;
	private static boolean serverHandlerWarned;

	@Override
	public Type<CustomDataPacket> getType() {
		return DEFINITION.type();
	}

	@Override
	public PacketBounds getBoundTo() {
		return DEFINITION.bounds();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, CustomDataPacket> getCodec() {
		return DEFINITION.codec();
	}

	@Override
	public void handleClient(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var handler = CLIENT_HANDLER;
			if (handler != null) {
				handler.accept(this, ctx);
			} else if (!clientHandlerWarned) {
				clientHandlerWarned = true;
				Common.LOGGER.warn("CustomDataPacket: client handler not initialized; dropping packet");
			}
			if (ctx.player() != null) {
				NeoForge.EVENT_BUS.post(new CustomDataReceivedEvent(ctx.player(), id, data));
			}
		});
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			var handler = SERVER_HANDLER;
			if (handler != null) {
				handler.accept(this, ctx);
			} else if (!serverHandlerWarned) {
				serverHandlerWarned = true;
				Common.LOGGER.warn("CustomDataPacket: server handler not initialized; dropping packet");
			}
			if (ctx.player() != null) {
				NeoForge.EVENT_BUS.post(new CustomDataReceivedEvent(ctx.player(), id, data));
			}
		});
	}
}
