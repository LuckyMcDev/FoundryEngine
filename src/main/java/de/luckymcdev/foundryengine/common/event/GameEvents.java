package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.game.GameSessionEvent;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

public class GameEvents {
	private static final EventGroupHolder<GameSessionEvent.Starting> STARTING = new EventGroupHolder<>();
	private static final EventGroupHolder<GameSessionEvent.Started> STARTED = new EventGroupHolder<>();
	private static final EventGroupHolder<GameSessionEvent.Stopping> STOPPING = new EventGroupHolder<>();
	private static final EventGroupHolder<GameSessionEvent.Stopped> STOPPED = new EventGroupHolder<>();

	public static void onStarting(EventCallback<GameSessionEvent.Starting> cb) {
		STARTING.register(cb);
	}

	public static void onStarted(EventCallback<GameSessionEvent.Started> cb) {
		STARTED.register(cb);
	}

	public static void onStopping(EventCallback<GameSessionEvent.Stopping> cb) {
		STOPPING.register(cb);
	}

	public static void onStopped(EventCallback<GameSessionEvent.Stopped> cb) {
		STOPPED.register(cb);
	}

	@ApiStatus.Internal
	public static class Internal {
		static {
			Common.registerEventClear(Internal::clear);
		}

		public static void postStarting(GameSessionEvent.Starting e) {
			STARTING.post(e);
		}

		public static void postStarted(GameSessionEvent.Started e) {
			STARTED.post(e);
		}

		public static void postStopping(GameSessionEvent.Stopping e) {
			STOPPING.post(e);
		}

		public static void postStopped(GameSessionEvent.Stopped e) {
			STOPPED.post(e);
		}

		public static void register(IEventBus bus) {
			bus.addListener(Internal::postStarting);
			bus.addListener(Internal::postStarted);
			bus.addListener(Internal::postStopping);
			bus.addListener(Internal::postStopped);
		}

		public static void clear() {
			STARTING.clear();
			STARTED.clear();
			STOPPING.clear();
			STOPPED.clear();
		}
	}
}
