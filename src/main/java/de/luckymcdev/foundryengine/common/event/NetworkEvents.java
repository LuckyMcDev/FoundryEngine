package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.event.data.CustomDataReceivedEvent;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus;

public class NetworkEvents {
	public static final EventGroupHolder<CustomDataReceivedEvent> CUSTOM_DATA_RECEIVED = new EventGroupHolder<>();

	public static void onCustomDataReceived(EventCallback<CustomDataReceivedEvent> cb) {
		CUSTOM_DATA_RECEIVED.register(cb);
	}

	@ApiStatus.Internal
	public static class Internal {
		static {
			Common.registerEventClear(Internal::clear);
		}

		public static void postCustomDataReceived(CustomDataReceivedEvent e) {
			CUSTOM_DATA_RECEIVED.post(e);
		}

		public static void register(IEventBus bus) {
			bus.addListener(Internal::postCustomDataReceived);
		}

		public static void clear() {
			CUSTOM_DATA_RECEIVED.clear();
		}
	}
}
