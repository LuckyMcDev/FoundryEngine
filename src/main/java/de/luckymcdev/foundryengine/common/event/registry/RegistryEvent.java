package de.luckymcdev.foundryengine.common.event.registry;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ToolMaterialBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.tag.TagBuilder;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Consumer;

public class RegistryEvent extends Event implements IModBusEvent {
	private final RegisterEvent inner;
	private final RegistryCollector collector;

	public RegistryEvent(RegisterEvent inner, RegistryCollector collector) {
		this.inner = inner;
		this.collector = collector;
	}

	public void items(ItemBuilder... builders) {
		inner.register(BuiltInRegistries.ITEM.key(), helper -> {
			for (ItemBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (ItemBuilder builder : builders) {
			collector.addItem(builder);
		}
	}

	public void toolMaterials(ToolMaterialBuilder... builders) {
		for (ToolMaterialBuilder builder : builders) {
			collector.addToolMaterial(builder);
		}
	}

	public void blocks(BlockBuilder... builders) {
		inner.register(BuiltInRegistries.BLOCK.key(), helper -> {
			for (BlockBuilder builder : builders) {
				builder.registerBlock(helper);
			}
		});
		for (BlockBuilder builder : builders) {
			collector.addBlock(builder);
		}

		List<BlockBuilder> withItem = List.of(builders).stream()
			.filter(BlockBuilder::hasItem)
			.toList();
		if (!withItem.isEmpty()) {
			inner.register(BuiltInRegistries.ITEM.key(), helper -> {
				for (BlockBuilder builder : withItem) {
					builder.registerItem(helper);
				}
			});
		}
	}

	public void recipes(RecipeBuilder... builders) {
		for (RecipeBuilder builder : builders) {
			collector.addRecipe(builder);
		}
	}

	public void particles(ParticleBuilder... builders) {
		inner.register(BuiltInRegistries.PARTICLE_TYPE.key(), helper -> {
			for (ParticleBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (ParticleBuilder builder : builders) {
			collector.addParticle(builder);
		}
	}

	public void sounds(SoundBuilder... builders) {
		inner.register(BuiltInRegistries.SOUND_EVENT.key(), helper -> {
			for (SoundBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (SoundBuilder builder : builders) {
			collector.addSound(builder);
		}
	}

	public void tags(TagBuilder<?>... builders) {
		for (TagBuilder<?> builder : builders) {
			collector.addTag(builder);
		}
	}

	public <T> void register(ResourceKey<Registry<T>> key, Consumer<RegisterEvent.RegisterHelper<T>> action) {
		inner.register(key, action);
	}
}