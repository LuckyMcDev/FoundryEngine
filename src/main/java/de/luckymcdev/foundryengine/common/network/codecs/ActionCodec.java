package de.luckymcdev.foundryengine.common.network.codecs;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public final class ActionCodec {

	private ActionCodec() {
	}

	public static <E extends Enum<E>> Function<Integer, E> toEnum(E[] values, E fallback) {
		return ordinal -> (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : fallback;
	}

	public static <E extends Enum<E>> StreamCodec<ByteBuf, E> streamCodec(E[] values, E fallback) {
		return ByteBufCodecs.VAR_INT.map(toEnum(values, fallback), Enum::ordinal);
	}
}
