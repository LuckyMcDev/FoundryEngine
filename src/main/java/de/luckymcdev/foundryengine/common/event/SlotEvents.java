package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.ApiStatus;

public class SlotEvents {
    public static final EventGroupHolder<AbstractContainerMenu> MODIFICATION = new EventGroupHolder<>();

    public static void modification(EventCallback<AbstractContainerMenu> callback) {
        MODIFICATION.register(callback);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void register(IEventBus bus) {
            bus.addListener(Internal::onContainerOpen);
            bus.addListener(Internal::onScreenInit);
        }

        private static void onContainerOpen(PlayerContainerEvent.Open event) {
            MODIFICATION.post(event.getContainer());
        }

        private static void onScreenInit(ScreenEvent.Init.Post event) {
            var screen = event.getScreen();
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    MODIFICATION.post(containerScreen.getMenu());
                }
            }
        }

        public static void clear() {
            MODIFICATION.clear();
        }

        static {
            Common.registerEventClear(Internal::clear);
        }
    }
}
