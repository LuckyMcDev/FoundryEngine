package de.luckymcdev.foundryengine.client.tooltip;

import com.mojang.serialization.DynamicOps;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.LinkedHashMap;
import java.util.List;

public class TooltipManager {

	public void handleItemTooltip(ItemTooltipEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		var flags = event.getFlags();

		if (!flags.isAdvanced()) {
			return;
		}

		var stack = event.getItemStack();

		if (stack.isEmpty()) {
			return;
		}

		var registryAccess = mc.level.registryAccess();

		var lines = event.getToolTip();

		if (event.getFlags().hasAltDown()) {
			handleComponents(registryAccess, stack, lines, event);
		} else if (event.getFlags().hasShiftDown()) {
			var fuel = stack.getBurnTime(null, mc.level.fuelValues());
			if (fuel > 0) {
				handleFuel(fuel, lines);
			}

			handleTags(event, stack, lines);
		}
	}

	private void handleComponents(RegistryAccess registryAccess, ItemStack stack, List<Component> lines, ItemTooltipEvent event) {
		var components = BuiltInRegistries.DATA_COMPONENT_TYPE;
		var ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);

		for (var entry : stack.getComponentsPatch().entrySet()) {
			var id = components.getKey(entry.getKey());

			if (id != null) {
				var line = Component.empty();
				line.append(ChatIcons.PATCHED_COMPONENT);
				line.append(ChatIcons.SMALL_SPACE);

				if (entry.getValue().isEmpty()) {
					line.append(Component.literal("!"));
				}

				line.append(Component.literal(reduce(id)).withStyle(ChatFormatting.YELLOW));

				if (entry.getValue().isPresent()) {
					line.append(Component.literal("="));
					var errors0 = appendComponentValue(ops, line, (DataComponentType) entry.getKey(), entry.getValue().get());

					if (!errors0.isEmpty()) {
						lines.add(Component.literal(reduce(id) + " errored, see log").withStyle(ChatFormatting.DARK_RED));
					}
				}

				lines.add(line);
			}
		}

		if (event.getFlags().hasShiftDown()) {
			for (var type : stack.getPrototype()) {
				var id = components.getKey(type.type());

				if (id != null && stack.getComponentsPatch().getPatch(type.type()) == null) {
					var line = Component.empty();
					line.append(ChatIcons.PROTOTYPE_COMPONENT);
					line.append(ChatIcons.SMALL_SPACE);
					line.append(Component.literal(reduce(id)).withStyle(ChatFormatting.GRAY));
					line.append(Component.literal("="));
					var errors0 = appendComponentValue(ops, line, (DataComponentType) type.type(), type.value());

					if (!errors0.isEmpty()) {
						lines.add(Component.literal(reduce(id) + " errored, see log").withStyle(ChatFormatting.DARK_RED));
					}

					lines.add(line);
				}
			}
		}
	}

	private void handleFuel(int fuel, List<Component> lines) {
		var line = Component.empty();
		line.append(ChatIcons.FIRE);
		line.append(ChatIcons.SMALL_SPACE);
		var txt = Component.empty().withStyle(ChatFormatting.GOLD);
		txt.append("Fuel: ");

		var s = String.valueOf(fuel / 20.0F);
		txt.append(Component.literal(fuel + " t").withStyle(ChatFormatting.YELLOW));
		txt.append(" | ");
		txt.append(Component.literal((s.endsWith(".0") ? s.substring(0, s.length() - 2) : s) + " s").withStyle(ChatFormatting.YELLOW));
		txt.append(" | ");

		var i = String.valueOf(fuel / 200.0F);
		txt.append(Component.literal((i.endsWith(".0") ? s.substring(0, i.length() - 2) : i) + "x").withStyle(ChatFormatting.YELLOW));

		line.append(txt);
		lines.add(line);
	}

	private void handleTags(ItemTooltipEvent event, ItemStack stack, List<Component> lines) {
		var tempTagNames = new LinkedHashMap<Identifier, TagInstance>();
		var gIconsEvent = new GatherItemTagIconsEvent(event, tempTagNames);

		gIconsEvent.append(TooltipTagType.ITEM, stack.getItem().builtInRegistryHolder());

		if (stack.getItem() instanceof BlockItem item) {
			gIconsEvent.append(TooltipTagType.BLOCK, item.getBlock().builtInRegistryHolder());
		}

		if (stack.getItem() instanceof BucketItem bucket) {
			var fluid = bucket.content;

			if (fluid != Fluids.EMPTY) {
				gIconsEvent.append(TooltipTagType.FLUID, fluid.builtInRegistryHolder());
			}
		}

		if (stack.getItem() instanceof SpawnEggItem) {
			var entityType = SpawnEggItem.getType(stack);

			if (entityType != null) {
				gIconsEvent.append(TooltipTagType.ENTITY_TYPE, entityType.builtInRegistryHolder());
			}
		}

		var enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);

		if (enchantments != null && enchantments.size() == 1) {
			gIconsEvent.append(TooltipTagType.ENCHANTMENT, enchantments.entrySet().iterator().next().getKey());
		}

		var instrumentComponent = stack.get(DataComponents.INSTRUMENT);

		if (instrumentComponent != null) {
			var instrument = instrumentComponent.instrument();
			gIconsEvent.append(TooltipTagType.INSTRUMENT, instrument);
		}

		var paintingVariant = stack.get(DataComponents.PAINTING_VARIANT);

		if (paintingVariant != null) {
			gIconsEvent.append(TooltipTagType.PAINTING_VARIANT, paintingVariant);
		}

		var bannerPattern = stack.get(DataComponents.PROVIDES_BANNER_PATTERNS);

		if (bannerPattern != null) {
			gIconsEvent.append(TooltipTagType.BANNER_PATTERN, bannerPattern);
		}

		NeoForge.EVENT_BUS.post(gIconsEvent);

		if (!tempTagNames.isEmpty()) {
			tempTagNames.values().stream().sorted().map(TagInstance::toText).forEach(lines::add);
		}
	}

	private <T> List<String> appendComponentValue(DynamicOps<Tag> ops, MutableComponent line, DataComponentType<T> type, T value) {
		if (value == null) {
			line.append(Component.literal("null").withStyle(ChatFormatting.RED));
			return List.of();
		} else if (value instanceof Component c) {
			line.append(Component.empty().withStyle(ChatFormatting.GOLD).append(c));
		}

		try {
			var tag = type.codecOrThrow().encodeStart(ops, value).getOrThrow();
			line.append(NbtUtils.toPrettyComponent(tag));
			return List.of();
		} catch (Throwable ex) {
			line.append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.RED));
			return List.of();
		}
	}

	private String reduce(Identifier id) {
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
	}
}
