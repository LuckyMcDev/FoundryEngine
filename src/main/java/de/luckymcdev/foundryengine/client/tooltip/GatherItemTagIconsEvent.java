package de.luckymcdev.foundryengine.client.tooltip;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;
import java.util.stream.Stream;

public class GatherItemTagIconsEvent extends Event {
	private final ItemTooltipEvent parentEvent;
	private final Map<Identifier, TagInstance> map;

	public GatherItemTagIconsEvent(ItemTooltipEvent parentEvent, Map<Identifier, TagInstance> map) {
		this.parentEvent = parentEvent;
		this.map = map;
	}

	public ItemTooltipEvent getParentEvent() {
		return parentEvent;
	}

	public ItemStack getItem() {
		return parentEvent.getItemStack();
	}

	public <T> void append(TooltipTagType<T> type, Stream<? extends TagKey<T>> tags) {
		tags.forEach(tag -> map.computeIfAbsent(tag.location(), TagInstance::new).registries.add(type));
	}

	public <T> void append(TooltipTagType<T> type, Holder<T> holder) {
		append(type, holder.tags());
	}

	public <T> void append(TooltipTagType<T> type, HolderSet<T> holderSet) {
		holderSet.unwrap().map(tagKey -> {
			append(type, Stream.of(tagKey));
			return null;
		}, holders -> {
			for (var holder : holders) {
				append(type, holder);
			}

			return null;
		});
	}
}
