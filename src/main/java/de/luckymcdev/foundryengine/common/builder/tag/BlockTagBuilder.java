package de.luckymcdev.foundryengine.common.builder.tag;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class BlockTagBuilder extends TagBuilder<Block> {
	protected BlockTagBuilder(Identifier id) {
		super(id, Registries.BLOCK);
	}

	public static BlockTagBuilder create(Identifier id) {
		return new BlockTagBuilder(id);
	}

	@Override
	public BlockTagBuilder add(ResourceKey<Block> key) {
		return (BlockTagBuilder) super.add(key);
	}

	@Override
	public BlockTagBuilder addOptional(ResourceKey<Block> key) {
		return (BlockTagBuilder) super.addOptional(key);
	}

	@Override
	public BlockTagBuilder addTag(TagKey<Block> tag) {
		return (BlockTagBuilder) super.addTag(tag);
	}

	@Override
	public BlockTagBuilder addOptionalTag(TagKey<Block> tag) {
		return (BlockTagBuilder) super.addOptionalTag(tag);
	}

	@Override
	public BlockTagBuilder remove(ResourceKey<Block> key) {
		return (BlockTagBuilder) super.remove(key);
	}

	@Override
	public BlockTagBuilder removeTag(TagKey<Block> tag) {
		return (BlockTagBuilder) super.removeTag(tag);
	}

	@Override
	public BlockTagBuilder replace() {
		return (BlockTagBuilder) super.replace();
	}

	@Override
	public BlockTagBuilder replace(boolean replace) {
		return (BlockTagBuilder) super.replace(replace);
	}

	public BlockTagBuilder add(Block block) {
		return add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
	}

	public BlockTagBuilder addOptional(Block block) {
		return addOptional(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
	}

	public BlockTagBuilder remove(Block block) {
		return remove(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
	}
}
