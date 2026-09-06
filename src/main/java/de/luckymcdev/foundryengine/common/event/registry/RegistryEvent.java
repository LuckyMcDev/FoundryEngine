package de.luckymcdev.foundryengine.common.event.registry;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.blockentity.BlockEntityBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ToolMaterialBuilder;
import de.luckymcdev.foundryengine.common.builder.menu.MenuBuilder;
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.tag.TagBuilder;
import de.luckymcdev.foundryengine.common.registry.RegistryCollector;
import de.luckymcdev.foundryengine.config.StartupConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistryEvent extends Event implements IModBusEvent {
	private static final Set<String> LOGGED_SKIPS = ConcurrentHashMap.newKeySet();

	static {
		Common.registerEventClear(LOGGED_SKIPS::clear);
	}

	private final Map<ResourceKey<? extends Registry<?>>, RegisterEvent> eventMap;
	private final RegistryCollector collector;

	public RegistryEvent(Map<ResourceKey<? extends Registry<?>>, RegisterEvent> eventMap,
	                     RegistryCollector collector) {
		this.eventMap = eventMap;
		this.collector = collector;
	}

	private static List<String> contentIds(AbstractBuilder<?>... builders) {
		return Stream.of(builders)
			.map(builder -> builder.getId().toString())
			.toList();
	}

	@SuppressWarnings("unchecked")
	private <T> void registerInner(ResourceKey<? extends Registry<T>> key,
	                               Consumer<RegisterEvent.RegisterHelper<T>> action) {
		RegisterEvent inner = eventMap.get(key);
		if (inner != null) {
			inner.register(key, action);
		}
	}

	public void items(ItemBuilder... builders) {
		if (blockContent("item", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.ITEM.key(), helper -> {
			for (ItemBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (ItemBuilder builder : builders) {
			collector.addItem(builder);
			builder.getTags().forEach(collector::addTag);
		}
	}

	public void toolMaterials(ToolMaterialBuilder... builders) {
		if (blockContent("tool material", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		for (ToolMaterialBuilder builder : builders) {
			collector.addToolMaterial(builder);
		}
	}

	public void blocks(BlockBuilder... builders) {
		if (blockContent("block", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.BLOCK.key(), helper -> {
			for (BlockBuilder builder : builders) {
				builder.registerBlock(helper);
			}
		});
		for (BlockBuilder builder : builders) {
			collector.addBlock(builder);
			builder.getTags().forEach(collector::addTag);
		}

		List<BlockBuilder> withItem = Stream.of(builders)
			.filter(BlockBuilder::hasItem)
			.toList();
		if (!withItem.isEmpty()) {
			registerInner(BuiltInRegistries.ITEM.key(), helper -> {
				for (BlockBuilder builder : withItem) {
					builder.registerItem(helper);
				}
			});
		}

		List<BlockEntityBuilder<?>> attachedBes = Stream.of(builders)
			.map(BlockBuilder::getBlockEntityBuilder)
			.filter(Objects::nonNull)
			.toList();
		if (!attachedBes.isEmpty()) {
			blockEntities(attachedBes.toArray(BlockEntityBuilder[]::new));
		}
	}

	public void menus(MenuBuilder<?>... builders) {
		if (blockContent("menu", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.MENU.key(), helper -> {
			for (MenuBuilder<?> builder : builders) {
				builder.register(helper);
			}
		});
		for (MenuBuilder<?> builder : builders) {
			collector.addMenu(builder);
		}
	}

	public void recipes(RecipeBuilder... builders) {
		if (blockContent("recipe", Common.getCompatibilityMode().supportsServer(), "the server", () -> contentIds(builders))) {
			return;
		}
		for (RecipeBuilder builder : builders) {
			collector.addRecipe(builder);
		}
	}

	public void particles(ParticleBuilder... builders) {
		if (blockContent("particle", Common.getCompatibilityMode().supportsClient(), "the client", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.PARTICLE_TYPE.key(), helper -> {
			for (ParticleBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (ParticleBuilder builder : builders) {
			collector.addParticle(builder);
		}
	}

	public void sounds(SoundBuilder... builders) {
		if (blockContent("sound", Common.getCompatibilityMode().supportsClient(), "the client", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.SOUND_EVENT.key(), helper -> {
			for (SoundBuilder builder : builders) {
				builder.register(helper);
			}
		});
		for (SoundBuilder builder : builders) {
			collector.addSound(builder);
		}
	}

	public void blockEntities(BlockEntityBuilder<?>... builders) {
		if (blockContent("block entity", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		registerInner(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), helper -> {
			for (BlockEntityBuilder<?> builder : builders) {
				builder.register(helper);
			}
		});
		for (BlockEntityBuilder<?> builder : builders) {
			collector.addBlockEntity(builder);
		}
	}

	public void tags(TagBuilder<?>... builders) {
		if (blockContent("tag", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> contentIds(builders))) {
			return;
		}
		for (TagBuilder<?> builder : builders) {
			collector.addTag(builder);
		}
	}

	public <T> void register(ResourceKey<Registry<T>> key,
	                         Consumer<RegisterEvent.RegisterHelper<T>> action) {
		if (blockContent("registry", Common.getCompatibilityMode().requiresBothSides(), "both the client and the server", () -> List.of(key.identifier().toString()))) {
			return;
		}
		registerInner(key, action);
	}

	private boolean blockContent(String kind, boolean allowed, String requires, Supplier<List<String>> ids) {
		if (allowed) {
			return false;
		}
		List<String> idList = ids.get();
		if (LOGGED_SKIPS.add(kind + ":" + idList)) {
			var str = "Compatibility mode %s: cannot register %s content (requires %s). Skipping: %s".formatted(Common.getCompatibilityMode().getName(), kind, requires, idList);
			if (!StartupConfig.COMPATIBILITY_MODE_WARNING_SKIP.get()) {
				var loadingIssue = ModLoadingIssue.warning(str);
				ModLoader.addLoadingIssue(loadingIssue);
			}
			Common.LOGGER.warn(str);
		}
		return true;
	}
}