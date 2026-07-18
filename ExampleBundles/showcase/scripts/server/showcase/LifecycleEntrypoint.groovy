package showcase

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.EntityEvents
import de.luckymcdev.foundryengine.common.event.GameEvents
import de.luckymcdev.foundryengine.common.event.PlayerEvents
import de.luckymcdev.foundryengine.common.game.GameData
import de.luckymcdev.foundryengine.common.game.GameSession
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class LifecycleEntrypoint implements BundleEntrypoint {

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("showcase", path)
    }

    @Override
    void onLoad() {
        registerGameSession()
        registerEventListeners()
    }

    /** Creates an auto-starting game session with a periodic time broadcast. */
    private void registerGameSession() {
        def data = new GameData(id("showcase_data"))
        def session = new GameSession(id("showcase_world"), data)
            .autoStart(true)
            .onStarting { println "[Showcase] Game session starting!" }
            .onServerTick { server, level ->
                if (level.dayTime % 6000 == 0) {
                    def players = level.players()
                    if (!players.isEmpty()) {
                        server.getPlayerList().broadcastSystemMessage(
                            Component.literal("§8[§eShowcase§8] §7Time: §f${level.dayTime / 20} seconds"), false)
                    }
                }
            }

        Common.getGameManager().register(session)
    }

    /** Registers general lifecycle listeners. */
    private void registerEventListeners() {
        GameEvents.onStarted { event ->
            println "[Showcase] Session ${event.sessionId} started!"
        }

        PlayerEvents.loggedIn { event ->
            println "[Showcase] ${event.entity.name} joined the game"
        }

        PlayerEvents.loggedOut { event ->
            println "[Showcase] ${event.entity.name} left the game"
        }

        EntityEvents.death { event ->
            def entity = event.entity
            def source = event.source
            println "[Showcase] ${entity.name} was killed by ${source.displayName}"
        }
    }
}
