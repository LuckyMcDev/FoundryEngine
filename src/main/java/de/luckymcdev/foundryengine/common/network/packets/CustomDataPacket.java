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

	public static BiConsumer<CustomDataPacket, IPayloadContext> CLIENT_HANDLER = (pkt, ctx) -> {
	};
	public static BiConsumer<CustomDataPacket, IPayloadContext> SERVER_HANDLER = (pkt, ctx) -> {
	};
	public static final Definition<CustomDataPacket> DEFINITION = new Definition<>(
		AbstractPacket.createType(Common.id("custom_data")),
		PacketBounds.BOTH,
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, CustomDataPacket::id,
			ByteBufCodecs.COMPOUND_TAG, CustomDataPacket::data,
			CustomDataPacket::new
		),
		CustomDataPacket::handleClient,
		CustomDataPacket::handleServer
	);

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
			CLIENT_HANDLER.accept(this, ctx);
			if (ctx.player() != null) {
				NeoForge.EVENT_BUS.post(new CustomDataReceivedEvent(ctx.player(), id, data));
			}
		});
	}

	@Override
	public void handleServer(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			SERVER_HANDLER.accept(this, ctx);
			if (ctx.player() != null) {
				NeoForge.EVENT_BUS.post(new CustomDataReceivedEvent(ctx.player(), id, data));
			}
		});
	}
}
