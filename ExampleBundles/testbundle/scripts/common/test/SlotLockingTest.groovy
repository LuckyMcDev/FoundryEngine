package common.test

import de.luckymcdev.foundryengine.common.event.SlotEvents
import de.luckymcdev.foundryengine.common.slot.SlotCustomization
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.network.chat.Component

class SlotLockingTest implements BundleEntrypoint {

    @Override
    void onLoad() {
        SlotEvents.modification { menu ->
            def slots = menu.slots

            println "=== SlotLockingTest ==="
            println "Menu: ${menu.getClass().simpleName} (${slots.size()} slots)"

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
