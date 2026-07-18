package showcase

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.PlayerEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.waypoint.Waypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.server.level.ServerPlayer

class WaypointEntrypoint implements BundleEntrypoint {

    @Override
    void onUnload() {}

    @Override
    void onLoad() {
        PlayerEvents.loggedIn { event ->
            def player = event.entity as ServerPlayer
            def level = player.level()
            def dim = level.dimension()
            if (!Common.getWaypointManager().isLoaded(dim)) {
                [
                    new Waypoint("Spawn", "H", 0, 64, 0, new Color(255, 200, 0)),
                    new Waypoint("Village", "D", -200, 63, 150, new Color(100, 200, 255)),
                    new Waypoint("Portal", "F", 500, 64, -300, new Color(200, 50, 200))
                ].each { w -> Common.getWaypointManager().addWaypoint(level, w) }
            }
        }
    }
}
