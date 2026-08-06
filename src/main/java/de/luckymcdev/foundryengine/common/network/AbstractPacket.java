package de.luckymcdev.foundryengine.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Base interface for all network packets.
 */
public interface AbstractPacket<T extends AbstractPacket<T>> extends CustomPacketPayload {

	/**
	 * Maximum length for user-supplied strings carried in packet payloads.
	 */
	int MAX_STRING_LENGTH = 65535;

	/**
	 * Maximum length for large file/resource content payloads.
	 */
	int MAX_FILE_CONTENT_LENGTH = 262144;

	/**
	 * Maximum element count for list fields in packet payloads.
	 */
	int MAX_LIST_ELEMENT_COUNT = 4096;

	/**
	 * Maximum module count for area packets.
	 */
	int MAX_MODULE_COUNT = 256;

	/**
	 * Bounded NBT codec with a generous quota for large editor/user payloads.
	 */
	StreamCodec<ByteBuf, CompoundTag> GENEROUS_NBT_CODEC = ByteBufCodecs.compoundTagCodec(NbtAccounter::uncompressedQuota);

	static <T extends CustomPacketPayload> Type<T> createType(Identifier id) {
		return new Type<>(id);
	}

	/**
	 * The server-side player that sent the payload, or {@code null} when the payload was received
	 * on a side without a player (e.g. dedicated server startup or client-bound traffic).
	 */
	static @Nullable ServerPlayer serverPlayer(IPayloadContext ctx) {
		return ctx.player() instanceof ServerPlayer player ? player : null;
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

	record Definition<T extends AbstractPacket<T>>(
		Type<T> type,
		PacketBounds bounds,
		StreamCodec<RegistryFriendlyByteBuf, T> codec,
		@Nullable BiConsumer<T, IPayloadContext> clientHandler,
		@Nullable BiConsumer<T, IPayloadContext> serverHandler
	) {
	}
}