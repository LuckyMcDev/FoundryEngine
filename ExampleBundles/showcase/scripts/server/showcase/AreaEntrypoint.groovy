package showcase

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.module.AreaLeaveModule
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel

class AreaEntrypoint implements BundleEntrypoint {

	static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("showcase", path)
	}

	@Override
	void onLoad() {
		def manager = Common.getAreaManager()

		manager.registerModuleType(new ShowcaseEnterModule())
		manager.registerModuleType(new ShowcaseLeaveModule())
		manager.registerModuleType(new ShowcaseHealModule())

		def preset = AreaPreset.builder("showcase:healing_zone")
				.color(new Color(100, 200, 100))
				.module(id("showcase_enter"))
				.module(id("showcase_leave"))
				.module(id("showcase_heal"))
				.build()

		manager.registerPreset(preset)
	}

	@Override
	void onUnload() {}

	static class ShowcaseEnterModule implements AreaEnterModule {
		Identifier id() { return Identifier.fromNamespaceAndPath("showcase", "showcase_enter") }
		void onEnter(ServerPlayer player, Area area) {
			player.sendSystemMessage(
					Component.literal("§aEntered area: ${area.id()}"))
		}
	}

	static class ShowcaseLeaveModule implements AreaLeaveModule {
		Identifier id() { return Identifier.fromNamespaceAndPath("showcase", "showcase_leave") }
		void onLeave(ServerPlayer player, Area area) {
			player.sendSystemMessage(
					Component.literal("§cLeft area: ${area.id()}"))
		}
	}

	static class ShowcaseHealModule implements AreaTickModule {
		Identifier id() { return Identifier.fromNamespaceAndPath("showcase", "showcase_heal") }
		void tick(ServerLevel level, Area area) {
			level.players().each { p ->
				if (area.contains(p.blockPosition()) && p.tickCount % 40 == 0) {
					p.heal(0.5f)
				}
			}
		}
	}
}