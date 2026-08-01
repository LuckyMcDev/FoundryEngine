package de.luckymcdev.foundryengine.mixin.sound;

import de.luckymcdev.foundryengine.client.sound.AudioFormats;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(SoundManager.Preparations.class)
public class SoundManagerPreparationsMixin {

	@Shadow
	private Map<Identifier, Resource> soundCache;

	/**
	 * @author LuckyMcDev
	 * @reason list sound resources for every supported audio format, not just ogg
	 */
	@Overwrite
	private void listResources(ResourceManager resourceManager) {
		Map<Identifier, Resource> merged = new HashMap<>();
		for (AudioFormats.Supported format : AudioFormats.Supported.values()) {
			FileToIdConverter converter = new FileToIdConverter("sounds", format.extension());
			merged.putAll(converter.listMatchingResources(resourceManager));
		}
		this.soundCache = merged;
	}
}