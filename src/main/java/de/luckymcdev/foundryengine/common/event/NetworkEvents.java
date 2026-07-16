package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.data.CustomDataReceivedEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

public class NetworkEvents {
	public static final EventGroupHolder<PlayerEvent.PlayerLoggedInEvent> LOGIN = new EventGroupHolder<>();
	public static final EventGroupHolder<PlayerEvent.PlayerLoggedOutEvent> LOGOUT = new EventGroupHolder<>();
	public static final EventGroupHolder<CustomDataReceivedEvent> CUSTOM_DATA_RECEIVED = new EventGroupHolder<>();

	public static void login(EventCallback<PlayerEvent.PlayerLoggedInEvent> cb) {
		LOGIN.register(cb);
	}

	public static void logout(EventCallback<PlayerEvent.PlayerLoggedOutEvent> cb) {
		LOGOUT.register(cb);
	}

	public static void onCustomDataReceived(EventCallback<CustomDataReceivedEvent> cb) {
		CUSTOM_DATA_RECEIVED.register(cb);
	}

	@ApiStatus.Internal
	public static class Internal {
		static {
			Common.registerEventClear(Internal::clear);
		}

		public static void postLogin(PlayerEvent.PlayerLoggedInEvent e) {
			LOGIN.post(e);
		}

		public static void postLogout(PlayerEvent.PlayerLoggedOutEvent e) {
			LOGOUT.post(e);
		}

		public static void postCustomDataReceived(CustomDataReceivedEvent e) {
			CUSTOM_DATA_RECEIVED.post(e);
		}

		public static void register(IEventBus bus) {
			bus.addListener(Internal::postLogin);
			bus.addListener(Internal::postLogout);
			bus.addListener(Internal::postCustomDataReceived);
		}

		public static void clear() {
			LOGIN.clear();
			LOGOUT.clear();
			CUSTOM_DATA_RECEIVED.clear();
		}
	}
}
