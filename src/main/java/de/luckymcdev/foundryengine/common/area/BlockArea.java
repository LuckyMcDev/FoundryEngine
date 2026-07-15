package de.luckymcdev.foundryengine.common.area;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockArea extends Area {
	private static final double BLOCK_HALF = 0.5;

	private BlockPos pos;

	public BlockArea(Identifier id, BlockPos pos, ResourceKey<Level> dimension, Color color) {
		super(id, dimension, color);
		this.pos = pos;
	}

	public static BlockArea of(Identifier id, BlockPos pos, ResourceKey<Level> dimension, Color color) {
		return new BlockArea(id, pos, dimension, color);
	}

	public static BlockArea readFromNbt(CompoundTag tag) {
		Identifier id = Identifier.parse(tag.getString("id").orElse("foundryengine:unknown"));
		ResourceKey<Level> dimension = ResourceKey.create(
			Registries.DIMENSION,
			Identifier.parse(tag.getString("dimension").orElse("minecraft:overworld"))
		);
		int x = tag.getInt("blockX").orElse(0);
		int y = tag.getInt("blockY").orElse(0);
		int z = tag.getInt("blockZ").orElse(0);
		Color color = new Color(tag.getInt("color").orElse(DEFAULT_COLOR.argb()));
		return new BlockArea(id, new BlockPos(x, y, z), dimension, color);
	}

	public BlockPos pos() {
		return pos;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public AABB bounds() {
		return new AABB(pos);
	}

	@Override
	public boolean contains(GlobalPos position) {
		return position.dimension() == dimension() && position.pos().equals(pos);
	}

	@Override
	public boolean contains(BlockPos p) {
		return p.equals(pos);
	}

	@Override
	public boolean contains(Vec3 v) {
		return v.x >= pos.getX() && v.x < pos.getX() + 1 &&
			v.y >= pos.getY() && v.y < pos.getY() + 1 &&
			v.z >= pos.getZ() && v.z < pos.getZ() + 1;
	}

	@Override
	public boolean contains(double x, double y, double z) {
		return (int) Math.floor(x) == pos.getX() &&
			(int) Math.floor(y) == pos.getY() &&
			(int) Math.floor(z) == pos.getZ();
	}

	@Override
	public CompoundTag writeToNbt() {
		CompoundTag tag = writeSharedNbt();
		tag.putString("type", "block");
		tag.putInt("blockX", pos.getX());
		tag.putInt("blockY", pos.getY());
		tag.putInt("blockZ", pos.getZ());
		return tag;
	}

	@Override
	public String toString() {
		return "BlockArea[" +
			"id=" + id() +
			", pos=" + pos +
			", dimension=" + dimension().identifier() +
			", color=" + color() +
			", modules=" + moduleIds() +
			']';
	}
}
