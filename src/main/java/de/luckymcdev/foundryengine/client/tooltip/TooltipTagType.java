package de.luckymcdev.foundryengine.client.tooltip;

import de.luckymcdev.foundryengine.common.util.ChatIcons;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Fluid;

public record TooltipTagType<T>(ResourceKey<? extends Registry<T>> registryKey, Component component) {

	public static final TooltipTagType<BannerPattern> BANNER_PATTERN = new TooltipTagType<>(Registries.BANNER_PATTERN, ChatIcons.Tags.BANNER_PATTERN);
	public static final TooltipTagType<Block> BLOCK = new TooltipTagType<>(Registries.BLOCK, ChatIcons.Tags.BLOCK);
	public static final TooltipTagType<Enchantment> ENCHANTMENT = new TooltipTagType<>(Registries.ENCHANTMENT, ChatIcons.Tags.ENCHANTMENT);
	public static final TooltipTagType<EntityType<?>> ENTITY_TYPE = new TooltipTagType<>(Registries.ENTITY_TYPE, ChatIcons.Tags.ENTITY_TYPE);
	public static final TooltipTagType<Fluid> FLUID = new TooltipTagType<>(Registries.FLUID, ChatIcons.Tags.FLUID);
	public static final TooltipTagType<Item> ITEM = new TooltipTagType<>(Registries.ITEM, ChatIcons.Tags.ITEM);
	public static final TooltipTagType<Instrument> INSTRUMENT = new TooltipTagType<>(Registries.INSTRUMENT, ChatIcons.Tags.INSTRUMENT);
	public static final TooltipTagType<PaintingVariant> PAINTING_VARIANT = new TooltipTagType<>(Registries.PAINTING_VARIANT, ChatIcons.Tags.PAINTING_VARIANT);

	@Override
	public int hashCode() {
		return registryKey.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof TooltipTagType<?> t && registryKey == t.registryKey;
	}

	@Override
	public String toString() {
		return registryKey.identifier().toString();
	}
}