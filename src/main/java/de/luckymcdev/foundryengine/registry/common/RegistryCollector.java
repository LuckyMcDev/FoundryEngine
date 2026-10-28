package de.luckymcdev.foundryengine.registry.common;

import de.luckymcdev.foundryengine.builder.common.block.BlockBuilder;
import de.luckymcdev.foundryengine.builder.common.blockentity.BlockEntityBuilder;
import de.luckymcdev.foundryengine.builder.common.item.ItemBuilder;
import de.luckymcdev.foundryengine.builder.common.item.ToolMaterialBuilder;
import de.luckymcdev.foundryengine.builder.common.menu.MenuBuilder;
import de.luckymcdev.foundryengine.builder.common.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.builder.common.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.builder.common.sound.SoundBuilder;
import de.luckymcdev.foundryengine.builder.common.tag.TagBuilder;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistryCollector {
	private final Map<Identifier, ItemBuilder> items = new LinkedHashMap<>();
	private final Map<Identifier, ToolMaterialBuilder> toolMaterials = new LinkedHashMap<>();
	private final Map<Identifier, BlockBuilder> blocks = new LinkedHashMap<>();
	private final Map<Identifier, ParticleBuilder> particles = new LinkedHashMap<>();
	private final Map<Identifier, SoundBuilder> sounds = new LinkedHashMap<>();
	private final Map<Identifier, RecipeBuilder> recipes = new LinkedHashMap<>();
	private final Map<Identifier, TagBuilder<?>> tags = new LinkedHashMap<>();
	private final Map<Identifier, BlockEntityBuilder<?>> blockEntities = new LinkedHashMap<>();
	private final Map<Identifier, MenuBuilder<?>> menus = new LinkedHashMap<>();

	public void addItem(ItemBuilder builder) {
		items.put(builder.getId(), builder);
	}

	public void addToolMaterial(ToolMaterialBuilder builder) {
		toolMaterials.put(builder.getId(), builder);
	}

	public void addBlock(BlockBuilder builder) {
		blocks.put(builder.getId(), builder);
	}

	public void addParticle(ParticleBuilder builder) {
		particles.put(builder.getId(), builder);
	}

	public void addSound(SoundBuilder builder) {
		sounds.put(builder.getId(), builder);
	}

	public void addRecipe(RecipeBuilder builder) {
		recipes.put(builder.getId(), builder);
	}

	public void addTag(TagBuilder<?> builder) {
		tags.put(builder.getId(), builder);
	}

	public void addBlockEntity(BlockEntityBuilder<?> builder) {
		blockEntities.put(builder.getId(), builder);
	}

	public void addMenu(MenuBuilder<?> builder) {
		menus.put(builder.getId(), builder);
	}

	public Collection<ItemBuilder> getItems() {
		return Collections.unmodifiableCollection(items.values());
	}

	public Collection<ToolMaterialBuilder> getToolMaterials() {
		return Collections.unmodifiableCollection(toolMaterials.values());
	}

	public Collection<BlockBuilder> getBlocks() {
		return Collections.unmodifiableCollection(blocks.values());
	}

	public Collection<ParticleBuilder> getParticles() {
		return Collections.unmodifiableCollection(particles.values());
	}

	public Collection<SoundBuilder> getSounds() {
		return Collections.unmodifiableCollection(sounds.values());
	}

	public Collection<RecipeBuilder> getRecipes() {
		return Collections.unmodifiableCollection(recipes.values());
	}

	public Collection<TagBuilder<?>> getTags() {
		return Collections.unmodifiableCollection(tags.values());
	}

	public Collection<BlockEntityBuilder<?>> getBlockEntities() {
		return Collections.unmodifiableCollection(blockEntities.values());
	}

	public Collection<MenuBuilder<?>> getMenus() {
		return Collections.unmodifiableCollection(menus.values());
	}
}