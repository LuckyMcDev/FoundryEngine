package de.luckymcdev.foundryengine.mixin.sound;

import de.luckymcdev.foundryengine.client.sound.AudioFormats;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

	@Shadow
	@Final
	private static Logger LOGGER;

	/**
	 * @author LuckyMcDev
	 * @reason allow non-ogg sound files to pass sounds.json validation
	 */
	@Overwrite
	private static boolean validateSoundResource(Sound sound, Identifier eventLocation, ResourceProvider resourceProvider) {
		Identifier soundPath = sound.getPath();
		if (AudioFormats.resolve(resourceProvider, soundPath).isEmpty()) {
			LOGGER.warn("File {} does not exist, cannot add it to event {}", soundPath, eventLocation);
			return false;
		}
		return true;
	}
}