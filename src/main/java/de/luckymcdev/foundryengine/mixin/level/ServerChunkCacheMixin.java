package de.luckymcdev.foundryengine.mixin.level;

import de.luckymcdev.foundryengine.interfaces.EngineLevelAccess;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents chunk tasks from executing when the level should not tick.
 */
@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow
	@Final
	ServerLevel level;

	/**
	 * Cancels pollTask when the owning level should not tick.
	 */
	@Inject(method = "pollTask", at = @At("HEAD"), cancellable = true)
	private void executeQueuedTasks(CallbackInfoReturnable<Boolean> ci) {
		if (!((EngineLevelAccess) this.level).engine$shouldTick()) {
			ci.setReturnValue(false);
		}
	}
}
