package de.luckymcdev.foundryengine.mixin.level;

import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {

	@Inject(method = "hasConfirmedExperimentalWarning", at = @At("HEAD"), cancellable = true)
	public void engine$hasConfirmedExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
		if (CommonConfig.SKIP_EXPERIMENTAL_WARNING.get()) {
			cir.setReturnValue(true);
		} else {
		}
	}
}
