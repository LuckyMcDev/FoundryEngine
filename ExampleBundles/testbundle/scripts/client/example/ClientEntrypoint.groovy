package client.example

import common.example.CommonEntrypoint
import de.luckymcdev.foundryengine.api.event.ClientEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class ClientEntrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        ClientEvents.tick {
            def player = Minecraft.getInstance().player
            if (player != null) {
                if (player.tickCount % 20 == 0) {
                    player.sendSystemMessage(Component.literal("Test + " + player.tickCount))
                }
            }
        }

        ClientEvents.renderGui {
            it.guiGraphics.fakeItem(new ItemStack(CommonEntrypoint.THIS_IS_A_ITEM.get()), 100, 200)
        }
    }

    @Override
    void onUnload() {
    }
}