package de.luckymcdev.foundryengine.mixin.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.luckymcdev.foundryengine.common.world.level.util.ChunkGeneratorSettingsProvider;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Redirects NoiseGeneratorSettings lookup to use the chunk generator's custom settings if available.
 */
@Mixin(ChunkMap.class)
public class ChunkMapMixin {

	/**
	 * Wraps the NoiseGeneratorSettings.dummy() call to use the chunk generator's provided settings.
	 */
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;dummy()Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;"))
	private NoiseGeneratorSettings engine$useProvidedChunkGeneratorSettings(Operation<NoiseGeneratorSettings> original, @Local(argsOnly = true) ChunkGenerator chunkGenerator) {
		if (chunkGenerator instanceof ChunkGeneratorSettingsProvider provider) {
			NoiseGeneratorSettings settings = provider.getSettings();
			if (settings != null) {
				return settings;
			}
		}

		return original.call();
	}
}
