package server.example

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.area.module.AreaBlockModule
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.module.AreaLeaveModule
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset
import de.luckymcdev.foundryengine.common.event.AreaEvents
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class ServerEntrypoint implements BundleEntrypoint {

    private static final Identifier MID_ENTER = Identifier.fromNamespaceAndPath("testbundle", "enter")
    private static final Identifier MID_LEAVE = Identifier.fromNamespaceAndPath("testbundle", "leave")
    private static final Identifier MID_TICK = Identifier.fromNamespaceAndPath("testbundle", "tick")
    private static final Identifier MID_PROTECT = Identifier.fromNamespaceAndPath("testbundle", "protect")

    @Override
    void onLoad() {
        Common.getAreaManager().registerModuleType(new AreaEnterModule() {
            @Override
            Identifier id() { return MID_ENTER }

            @Override
            void onEnter(ServerPlayer player, Area area) {
                player.sendSystemMessage(
                        Component.literal("§aEntered area: " + area.id()))
            }
        })

        Common.getAreaManager().registerModuleType(new AreaLeaveModule() {
            @Override
            Identifier id() { return MID_LEAVE }

            @Override
            void onLeave(ServerPlayer player, Area area) {
                player.sendSystemMessage(
                        Component.literal("§eLeft area: " + area.id()))
            }
        })

        Common.getAreaManager().registerModuleType(new AreaTickModule() {
            @Override
            Identifier id() { return MID_TICK }

            @Override
            void tick(ServerLevel level, Area area) {
                int interval = area.getModuleData(MID_TICK).getInt("interval").orElse(20)
                if (level.server.tickCount % interval != 0) return
                level.players().each { player ->
                    if (area.bounds().contains(player.position())) {
                        player.sendSystemMessage(
                                Component.literal("§7Tick area: " + area.id()))
                    }
                }
            }
        })

        Common.getAreaManager().registerModuleType(new AreaBlockModule() {
            @Override
            Identifier id() { return MID_PROTECT }

            @Override
            void onBlockBreak(ServerLevel level, Area area, BlockPos pos,
                              BlockState state, ServerPlayer player) {
                player.sendSystemMessage(Component.literal("§cBlock protected in area: " + area.id()))
            }
        })

        // Register a preset that bundles all four modules with default config
        def preset = AreaPreset.builder("test_preset")
                .module(MID_ENTER)
                .module(MID_LEAVE)
                .module(MID_TICK)
                .module(MID_PROTECT)
                .moduleData(MID_TICK, { it.putInt("interval", 40) })
                .build()
        Common.getAreaManager().registerPreset(preset)

        AreaEvents.register { ServerLevel level ->
            if (level.dimension() != Level.OVERWORLD) return

            // Test 1: create area directly (manual module attachment)
            def area = Area.of(Identifier.fromNamespaceAndPath("testbundle", "test_area"),
                    new Vec3(-10, 60, -10),
                    new Vec3(10, 70, 10),
                    level.dimension(),
                    Color.ORANGE)
            area.addModule(MID_ENTER)
            area.addModule(MID_LEAVE)
            area.addModule(MID_TICK)
            area.addModule(MID_PROTECT)
            Common.getAreaManager().register(level, area)
            level.server.sendSystemMessage(
                    Component.literal("§a[Test] Area 'test_area' created at spawn (direct)"))

            // Test 2: create area from preset (automatic module attachment + default data)
            def zone = preset.create(
                    Identifier.fromNamespaceAndPath("testbundle", "preset_zone"),
                    new Vec3(15, 60, -10),
                    new Vec3(25, 70, 10),
                    level.dimension(),
                    Color.LIME)
            Common.getAreaManager().register(level, zone)
            level.server.sendSystemMessage(
                    Component.literal("§a[Test] Area 'preset_zone' created at spawn+15 (from preset)" +
                            " — tick interval=" + zone.getModuleData(MID_TICK).getInt("interval")))
        }
    }

    @Override
    void onUnload() {
    }
}
