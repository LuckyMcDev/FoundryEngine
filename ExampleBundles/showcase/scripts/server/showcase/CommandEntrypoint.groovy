package showcase

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.CommandEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class CommandEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onUnload() {}

    @Override
    void onLoad() {
        CommandEvents.register { event ->
            def dispatcher = event.getDispatcher()

            dispatcher.register(
                Commands.literal("showcase")
                    .then(Commands.literal("hello")
                        .executes { ctx ->
                            ctx.getSource().sendSuccess(
                                Component.literal("§eHello from the Showcase bundle!"), false)
                            Command.SINGLE_SUCCESS
                        })
                    .then(Commands.literal("stage")
                        .then(Commands.argument("stage", StringArgumentType.word())
                            .executes { ctx ->
                                def player = ctx.getSource().getPlayerOrException()
                                def stageId = Identifier.fromNamespaceAndPath("showcase", StringArgumentType.getString(ctx, "stage"))
                                if (Common.getGameStageHandler().hasStage(player, stageId)) {
                                    ctx.getSource().sendSuccess(
                                        Component.literal("§aYou have stage: ${stageId.path}"), false)
                                } else {
                                    ctx.getSource().sendSuccess(
                                        Component.literal("§cYou do not have stage: ${stageId.path}"), false)
                                }
                                Command.SINGLE_SUCCESS
                            }))
                    .then(Commands.literal("list_waypoints")
                        .executes { ctx ->
                            def player = ctx.getSource().getPlayerOrException()
                            def waypoints = Common.getWaypointManager()
                                .getWaypoints(player.level().dimension())
                            waypoints.each { w ->
                                ctx.getSource().sendSuccess(
                                    Component.literal("§7- §e${w.name()} §7at (${w.x()}, ${w.y()}, ${w.z()})"), false)
                            }
                            Command.SINGLE_SUCCESS
                        })
                    .then(Commands.literal("dialogue")
                        .executes { ctx ->
                            def player = ctx.getSource().getPlayerOrException()
                            Common.getDialogueManager().startDialogue(player, id("welcome_guide"))
                            Command.SINGLE_SUCCESS
                        })
            )
        }
    }
}
