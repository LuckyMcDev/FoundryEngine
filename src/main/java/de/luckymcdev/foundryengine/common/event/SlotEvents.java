package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.ApiStatus;

public class SlotEvents {
	public static final EventGroupHolder<AbstractContainerMenu> MODIFICATION = new EventGroupHolder<>();

	public static void modification(EventCallback<AbstractContainerMenu> callback) {
		MODIFICATION.register(callback);
	}

	@ApiStatus.Internal
	public static class Internal {
		static {
			Common.registerEventClear(Internal::clear);
		}

		public static void register(IEventBus bus) {
			bus.addListener(Internal::onContainerOpen);
		}

		private static void onContainerOpen(PlayerContainerEvent.Open event) {
			MODIFICATION.post(event.getContainer());
		}

		public static void clear() {
			MODIFICATION.clear();
		}
	}
}
