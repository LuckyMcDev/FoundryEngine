package client.test

import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.slot.SlotCustomization
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.client.event.ScreenEvent

class SlotLockingClientTest implements BundleEntrypoint {

    @Override
    void onLoad() {
        BundleEvents.custom(ScreenEvent.Init.Post.class) { event ->
            def screen = event.screen
            if (!(screen instanceof AbstractContainerScreen)) return

            def slots = screen.menu.slots

            println "=== SlotLockingClientTest ==="
            println "Screen: ${screen.getClass().simpleName} (${slots.size()} slots)"

            def first = slots[0] as SlotCustomization
            first.engine$setDisabledOverride(true)
            first.engine$setSlotTooltipText([
                Component.literal("§c§lLOCKED"),
                Component.literal("§7This slot has been locked by the engine")
            ])

            def last = slots[-1] as SlotCustomization
            last.engine$setDisabledOverride(true)

            def mid = slots[slots.size().intdiv(2)] as SlotCustomization
            mid.engine$setSlotTooltipText([
                Component.literal("§e§lCUSTOM"),
                Component.literal("§7Usable slot with custom tooltip")
            ])

            println "Locked slot 0 (${slots[0].index}) and slot ${slots[-1].index}"
            println "Tooltip set on slot ${slots[slots.size().intdiv(2)].index}"
            println "========================="
        }
    }

    @Override
    void onUnload() {
    }
}
