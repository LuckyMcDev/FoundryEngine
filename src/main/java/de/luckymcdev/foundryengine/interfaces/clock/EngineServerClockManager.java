package de.luckymcdev.foundryengine.interfaces.clock;

import de.luckymcdev.foundryengine.common.exceptions.NoMixinException;
import de.luckymcdev.foundryengine.interfaces.EngineInterface;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;

import java.util.Map;

/**
 * Exposes the internal clock instances of a {@link ServerClockManager}.
 */
public interface EngineServerClockManager extends EngineInterface<ServerClockManager> {
	/**
	 * Returns the map of world clocks to their clock instances.
	 */
	default Map<Holder<WorldClock>, ServerClockManager.ClockInstance> engine$getClocks() {
		throw new NoMixinException(this);
	}
}
