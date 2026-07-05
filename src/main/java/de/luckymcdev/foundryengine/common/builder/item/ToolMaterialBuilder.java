package de.luckymcdev.foundryengine.common.builder.item;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public class ToolMaterialBuilder extends AbstractBuilder<ToolMaterial> {
	private TagKey<Block> incorrectBlocksForDrops = BlockTags.INCORRECT_FOR_WOODEN_TOOL;
	private int durability = 59;
	private float speed = 2.0F;
	private float attackDamageBonus = 0.0F;
	private int enchantmentValue = 15;
	private TagKey<Item> repairItems = ItemTags.WOODEN_TOOL_MATERIALS;

	protected ToolMaterialBuilder(Identifier id) {
		super(id);
	}

	public static ToolMaterialBuilder create(Identifier id) {
		return new ToolMaterialBuilder(id);
	}

	public ToolMaterialBuilder incorrectBlocksForDrops(TagKey<Block> tag) {
		this.incorrectBlocksForDrops = tag;
		return this;
	}

	public ToolMaterialBuilder durability(int durability) {
		this.durability = durability;
		return this;
	}

	public ToolMaterialBuilder speed(float speed) {
		this.speed = speed;
		return this;
	}

	public ToolMaterialBuilder attackDamageBonus(float attackDamageBonus) {
		this.attackDamageBonus = attackDamageBonus;
		return this;
	}

	public ToolMaterialBuilder enchantmentValue(int enchantmentValue) {
		this.enchantmentValue = enchantmentValue;
		return this;
	}

	public ToolMaterialBuilder repairItems(TagKey<Item> repairItems) {
		this.repairItems = repairItems;
		return this;
	}

	@Override
	protected ToolMaterial build() {
		return new ToolMaterial(incorrectBlocksForDrops, durability, speed, attackDamageBonus, enchantmentValue, repairItems);
	}
}
