package de.luckymcdev.foundryengine.client.tooltip;

import de.luckymcdev.foundryengine.common.util.ChatIcons;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

public class TagInstance implements Comparable<TagInstance> {
	public final Identifier tag;
	public final Set<TooltipTagType<?>> registries;

	public TagInstance(Identifier tag) {
		this.tag = tag;
		this.registries = new LinkedHashSet<>(2);
	}

	public Component toText() {
		var component = Component.empty();
		component.append(ChatIcons.TAG);
		component.append(ChatIcons.SMALL_SPACE);
		component.append(Component.literal("#" + tag).withStyle(ChatFormatting.DARK_GRAY));
		component.append(ChatIcons.SMALL_SPACE);

		for (var type : registries) {
			component.append(type.component());
		}

		return component;
	}

	@Override
	public int compareTo(TagInstance o) {
		return tag.compareNamespaced(o.tag);
	}
}
