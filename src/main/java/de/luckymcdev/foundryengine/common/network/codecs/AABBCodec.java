package de.luckymcdev.foundryengine.common.network.codecs;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public class AABBCodec implements StreamCodec<RegistryFriendlyByteBuf, AABB> {
	public static final AABBCodec INSTANCE = new AABBCodec();

	@Override
	public AABB decode(RegistryFriendlyByteBuf buf) {
		double minX = buf.readDouble();
		double minY = buf.readDouble();
		double minZ = buf.readDouble();
		double maxX = buf.readDouble();
		double maxY = buf.readDouble();
		double maxZ = buf.readDouble();
		return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void encode(RegistryFriendlyByteBuf buf, AABB aabb) {
		buf.writeDouble(aabb.minX);
		buf.writeDouble(aabb.minY);
		buf.writeDouble(aabb.minZ);
		buf.writeDouble(aabb.maxX);
		buf.writeDouble(aabb.maxY);
		buf.writeDouble(aabb.maxZ);
	}
}