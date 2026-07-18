package de.luckymcdev.foundryengine.mixin.clock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Redirects clock manager lookup to use the per-level clock instead of the server-level one in sendLevelInfo.
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

	/**
	 * Injects in sendLevelInfo to use the level-specific clock manager.
	 */
	@WrapOperation(method = "sendLevelInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
	private ServerClockManager replaceClockManager(MinecraftServer instance, Operation<ServerClockManager> original, @Local(argsOnly = true, name = "level") ServerLevel level) {
		return level.clockManager();
	}
}
