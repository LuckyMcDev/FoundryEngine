package de.luckymcdev.foundryengine.mixin.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import de.luckymcdev.foundryengine.client.sound.AudioFormats;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.FiniteAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundBufferLibrary.class)
public class SoundBufferLibraryMixin {

	@Shadow
	@Final
	private Map<Identifier, CompletableFuture<SoundBuffer>> cache;

	@Shadow
	@Final
	private ResourceProvider resourceManager;

	/**
	 * @author LuckyMcDev
	 * @reason resolve non-ogg extensions before opening the resource
	 */
	@Overwrite
	public CompletableFuture<SoundBuffer> getCompleteBuffer(Identifier location) {
		return this.cache.computeIfAbsent(location, l -> CompletableFuture.supplyAsync(() -> {
			try {
				Identifier resolved = AudioFormats.resolve(this.resourceManager, l).orElse(l);
				SoundBuffer x2;
				try (
					InputStream is = this.resourceManager.open(resolved);
					FiniteAudioStream as = AudioFormats.openFinite(is)
				) {
					ByteBuffer data = as.readAll();
					x2 = new SoundBuffer(data, as.getFormat());
				}

				return x2;
			} catch (IOException var10) {
				throw new CompletionException(var10);
			}
		}, Util.nonCriticalIoPool()));
	}

	/**
	 * @author LuckyMcDev
	 * @reason resolve non-ogg extensions before opening the resource
	 */
	@Overwrite
	public CompletableFuture<AudioStream> getStream(Identifier location, boolean looping) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				Identifier resolved = AudioFormats.resolve(this.resourceManager, location).orElse(location);
				InputStream is = this.resourceManager.open(resolved);
				return AudioFormats.open(is, looping);
			} catch (IOException var4) {
				throw new CompletionException(var4);
			}
		}, Util.nonCriticalIoPool());
	}
}