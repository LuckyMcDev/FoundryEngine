package de.luckymcdev.foundryengine.mixin.level;

import com.mojang.serialization.Lifecycle;
import de.luckymcdev.foundryengine.config.CommonConfig;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
	@ModifyVariable(
		method = "confirmWorldCreation",
		at = @At("HEAD"),
		argsOnly = true,
		name = "lifecycle"
	)
	private static Lifecycle engine$lifecycleAlwaysStable(Lifecycle lifecycle) {
		if (CommonConfig.SKIP_EXPERIMENTAL_WARNING.get()) {
			return Lifecycle.stable();
		}
		return lifecycle;
	}

	@ModifyVariable(
		method = "openWorldCheckWorldStemCompatibility",
		at = @At("STORE"),
		name = "unstable"
	)
	public boolean engine$setUnstableToFalse(boolean unstable) {
		if (CommonConfig.SKIP_EXPERIMENTAL_WARNING.get()) {
			return false;
		} else {
			return unstable;
		}
	}
}
