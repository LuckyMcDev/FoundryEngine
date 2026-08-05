package de.luckymcdev.foundryengine.mixin.clock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.luckymcdev.foundryengine.interfaces.clock.EngineServerClockManager;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Implements {@link EngineServerClockManager} to expose clock instances and redirect broadcasts per-level.
 */
@Mixin(ServerClockManager.class)
public abstract class ServerClockManagerMixin implements EngineServerClockManager {
	@Shadow
	@Final
	private Map<Holder<WorldClock>, ServerClockManager.ClockInstance> clocks;

	/**
	 * Returns the internal map of clocks.
	 */
	@Override
	public Map<Holder<WorldClock>, ServerClockManager.ClockInstance> engine$getClocks() {
		return this.clocks;
	}

	/**
	 * Wraps broadcastAll in modifyClock to only send time updates to players in this clock's level.
	 */
	@WrapOperation(method = "modifyClock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
	private void updateTimeOnlyOverworld(PlayerList instance, Packet<?> packet, Operation<Void> original) {
		for (ServerPlayer player : instance.getPlayers()) {
			if (player.level().clockManager() == (Object) this) {
				player.connection.send(packet);
			}
		}
	}
}
