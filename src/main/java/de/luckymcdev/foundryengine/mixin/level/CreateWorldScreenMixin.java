package de.luckymcdev.foundryengine.mixin.level;

import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {

	@ModifyVariable(
		method = "tryApplyNewDataPacks",
		at = @At("HEAD"),
		argsOnly = true,
		name = "isDataPackScreen"
	)
	public boolean dontShowWarning(boolean isDataPackScreen) {
		if (CommonConfig.SKIP_EXPERIMENTAL_WARNING.get()) {
			return false;
		} else {
			return isDataPackScreen;
		}
	}
}
