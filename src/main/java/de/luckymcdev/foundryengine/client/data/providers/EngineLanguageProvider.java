package de.luckymcdev.foundryengine.client.data.providers;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.sound.SoundBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.List;
import java.util.Locale;

public class EngineLanguageProvider extends LanguageProvider {
	private final String namespace;
	private final List<BlockBuilder> blockBuilders;
	private final List<ItemBuilder> itemBuilders;
	private final List<SoundBuilder> soundBuilders;

	public EngineLanguageProvider(PackOutput output, String locale, String namespace, List<BlockBuilder> blockBuilders, List<ItemBuilder> itemBuilders, List<SoundBuilder> soundBuilders) {
		super(output, namespace, locale);
		this.namespace = namespace;
		this.blockBuilders = blockBuilders;
		this.itemBuilders = itemBuilders;
		this.soundBuilders = soundBuilders;
	}

	private static String formatTitleCase(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		String[] words = input.split("_");
		StringBuilder builder = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				builder.append(Character.toUpperCase(word.charAt(0)))
					.append(word.substring(1).toLowerCase(Locale.ENGLISH))
					.append(" ");
			}
		}
		return builder.toString().trim();
	}

	@Override
	protected void addTranslations() {
		for (BlockBuilder builder : blockBuilders) {
			add(builder.get(), formatTitleCase(builder.getId().getPath()));
			if (builder.hasItem()) {
				add(builder.get().asItem(), formatTitleCase(builder.getId().getPath()));
			}
		}

		for (ItemBuilder builder : itemBuilders) {
			var item = builder.get();
			if (item instanceof BlockItem) {
				continue;
			}
			add(item, formatTitleCase(builder.getId().getPath()));
		}


		// this should i guess be removed as its kinda stupid?
		for (var type : BuiltInRegistries.ENTITY_TYPE) {
			Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
			if (!id.getNamespace().equals(namespace)) {
				continue;
			}
			add(type, formatTitleCase(id.getPath()));
		}

		for (var effect : BuiltInRegistries.MOB_EFFECT) {
			Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
			if (!id.getNamespace().equals(namespace)) {
				continue;
			}
			add(effect, formatTitleCase(id.getPath()));
		}

		for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
			Identifier id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
			if (!id.getNamespace().equals(namespace)) {
				continue;
			}
			add("itemGroup." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
		}

		for (var fluid : BuiltInRegistries.FLUID) {
			Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
			if (!id.getNamespace().equals(namespace)) {
				continue;
			}
			add("fluid." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
		}

		for (var potion : BuiltInRegistries.POTION) {
			Identifier id = BuiltInRegistries.POTION.getKey(potion);
			if (!id.getNamespace().equals(namespace)) {
				continue;
			}
			String name = formatTitleCase(id.getPath());
			add("item.minecraft.potion.effect." + id.getPath(), name);
			add("item.minecraft.splash_potion.effect." + id.getPath(), name);
			add("item.minecraft.lingering_potion.effect." + id.getPath(), name);
			add("item.minecraft.tipped_arrow.effect." + id.getPath(), name);
		}

		for (SoundBuilder builder : soundBuilders) {
			Identifier id = builder.getId();
			add("sound_event." + id.getNamespace() + "." + id.getPath(), formatTitleCase(id.getPath()));
		}
	}
}
