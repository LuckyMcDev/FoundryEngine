package de.luckymcdev.foundryengine.mixin.screen;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker to add entries to AbstractSelectionList.
 */
@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor<E extends AbstractSelectionList.Entry<E>> {
	@Invoker("addEntry")
	int invokeAddEntry(E entry);
}