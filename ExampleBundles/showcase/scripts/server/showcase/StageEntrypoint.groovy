package showcase

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.StageEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer

class StageEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onUnload() {}

    @Override
    void onLoad() {
        def registry = Common.getGameStageHandler().getStageRegistry()

        registry.register(
            id("beginner"),
            Component.literal("Beginner"),
            Component.literal("Just starting out"))

        registry.register(
            id("explorer"),
            Component.literal("Explorer"),
            Component.literal("Traveled far and wide"),
            [id("beginner")])

        registry.register(
            id("master"),
            Component.literal("Master"),
            Component.literal("Master of all trades"),
            [id("explorer")])

        StageEvents.adding { event ->
            println "[Showcase] ${event.entity.name} is gaining stage: ${event.stage}"
        }

        StageEvents.added { event ->
            def player = event.entity as ServerPlayer
            player.sendSystemMessage(
                Component.literal("§6New stage unlocked: §e${event.stage}"))
        }
    }
}
