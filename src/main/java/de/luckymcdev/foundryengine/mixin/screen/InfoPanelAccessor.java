package de.luckymcdev.foundryengine.mixin.screen;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Size2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Invoker to set info panel content on the mod screen.
 */
@Mixin(targets = "net.neoforged.neoforge.client.gui.ModListScreen$InfoPanel")
public interface InfoPanelAccessor {
	@Invoker("setInfo")
	void invokeSetInfo(List<String> lines, Identifier logoPath, Size2i logoDims);
}