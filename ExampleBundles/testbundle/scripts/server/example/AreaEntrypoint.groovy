package server.example

import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.AABBArea
import de.luckymcdev.foundryengine.common.area.Area
import de.luckymcdev.foundryengine.common.area.BlockArea
import de.luckymcdev.foundryengine.common.area.module.AreaBlockModule
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.module.AreaLeaveModule
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule
import de.luckymcdev.foundryengine.common.area.preset.AreaPreset
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.util.color.Color
import de.luckymcdev.foundryengine.server.Server
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.event.level.block.BreakBlockEvent

class AreaEntrypoint implements BundleEntrypoint {

    private static final Identifier MID_ENTER = Identifier.fromNamespaceAndPath("testbundle", "ae_enter")
    private static final Identifier MID_LEAVE = Identifier.fromNamespaceAndPath("testbundle", "ae_leave")
    private static final Identifier MID_TICK = Identifier.fromNamespaceAndPath("testbundle", "ae_tick")
    private static final Identifier MID_PROTECT = Identifier.fromNamespaceAndPath("testbundle", "ae_protect")
    private static final Identifier MID_TRIGGER = Identifier.fromNamespaceAndPath("testbundle", "ae_trigger")

    @Override
    void onLoad() {
        registerModules()
        registerPreset()

        var server = Server.getServer()
        if (server != null) {
            for (def level : server.getAllLevels()) {
                if (level.dimension() == Level.OVERWORLD) {
                    testAabbArea(level as ServerLevel)
                    testBlockArea(level as ServerLevel)
                    testLinkedAreas(level as ServerLevel)
                    testPresetArea(level as ServerLevel)
                }
            }
        }
    }

    private void registerModules() {
        Common.getAreaManager().registerModuleType(new AreaEnterModule() {
            @Override Identifier id() { return MID_ENTER }
            @Override void onEnter(ServerPlayer player, Area area) {
                player.sendSystemMessage(
                        Component.literal("§a[AreaTest] Entered: " + area.id()))
            }
        })

        Common.getAreaManager().registerModuleType(new AreaLeaveModule() {
            @Override Identifier id() { return MID_LEAVE }
            @Override void onLeave(ServerPlayer player, Area area) {
                player.sendSystemMessage(
                        Component.literal("§e[AreaTest] Left: " + area.id()))
            }
        })

        Common.getAreaManager().registerModuleType(new AreaTickModule() {
            @Override Identifier id() { return MID_TICK }
            @Override void tick(ServerLevel level, Area area) {
                int interval = area.getModuleData(MID_TICK).getInt("interval").orElse(20)
                if (level.server.tickCount % interval != 0) return
                level.players().each { player ->
                    if (area.bounds().contains(player.position())) {
                        player.sendSystemMessage(
                                Component.literal("§7[AreaTest] Tick: " + area.id()))
                    }
                }
            }
        })

        Common.getAreaManager().registerModuleType(new AreaBlockModule() {
            @Override Identifier id() { return MID_PROTECT }
            @Override void onBlockBreak(BreakBlockEvent event, ServerLevel level, Area area, BlockPos pos,
                                         BlockState state, ServerPlayer player) {
                player.sendSystemMessage(
                        Component.literal("§c[AreaTest] Blocks cannot be broken broken in: " + area.id()))

                event.cancel()
            }
        })
    }

    private void registerPreset() {
        def preset = AreaPreset.builder("area_test_preset")
                .module(MID_ENTER)
                .module(MID_LEAVE)
                .moduleData(MID_TICK, { it.putInt("interval", 60) })
                .build()
        Common.getAreaManager().registerPreset(preset)
    }

    // ── Tests ────────────────────────────────────────────────────────

    private void testAabbArea(ServerLevel level) {
        def area = AABBArea.of(
                Identifier.fromNamespaceAndPath("testbundle", "aabb_test"),
                new Vec3(-10, 60, -10),
                new Vec3(10, 80, 10),
                level.dimension(),
                Color.ORANGE)
        area.addModule(MID_ENTER)
        area.addModule(MID_LEAVE)
        area.addModule(MID_TICK)
        area.addModule(MID_PROTECT)
        area.addModule(MID_TRIGGER)
        Common.getAreaManager().register(level, area)
        log(level, "AABBArea 'aabb_test' at spawn (-10..10)")
    }

    private void testBlockArea(ServerLevel level) {
        def area = BlockArea.of(
                Identifier.fromNamespaceAndPath("testbundle", "block_test"),
                new BlockPos(15, 65, 0),
                level.dimension(),
                Color.LIME)
        area.addModule(MID_ENTER)
        area.addModule(MID_TRIGGER)
        Common.getAreaManager().register(level, area)
        log(level, "BlockArea 'block_test' at (15, 65, 0)")

        // verify contains
        assert area.contains(new BlockPos(15, 65, 0)) : "BlockArea should contain its position"
        assert !area.contains(new BlockPos(15, 66, 0)) : "BlockArea should not contain adjacent position"
    }

    private void testLinkedAreas(ServerLevel level) {
        // Create two areas and link them together
        def source = AABBArea.of(
                Identifier.fromNamespaceAndPath("testbundle", "link_source"),
                new Vec3(30, 60, -10),
                new Vec3(40, 70, 10),
                level.dimension(),
                Color.MAGENTA)
        source.addModule(MID_ENTER)
        source.addModule(MID_LEAVE)

        def target = AABBArea.of(
                Identifier.fromNamespaceAndPath("testbundle", "link_target"),
                new Vec3(50, 60, -10),
                new Vec3(60, 70, 10),
                level.dimension(),
                Color.CYAN)
        target.addModule(MID_ENTER)

        // Link: source -> target by name "sibling"
        source.linkArea("sibling", target.id())

        // Link: target -> source by name "sibling"
        target.linkArea("sibling", source.id())

        Common.getAreaManager().register(level, source)
        Common.getAreaManager().register(level, target)

        // Verify links
        Identifier linked1 = source.getLinkedArea("sibling")
        assert linked1 != null : "source should have link 'sibling'"
        assert linked1 == target.id() : "source.sibling should point to target"

        Identifier linked2 = target.getLinkedArea("sibling")
        assert linked2 != null : "target should have link 'sibling'"
        assert linked2 == source.id() : "target.sibling should point to source"

        // Resolve via manager
        Area resolved = Common.getAreaManager().getArea(linked1)
        assert resolved != null : "linked area should exist in manager"
        assert resolved.id() == target.id() : "resolved area should be target"

        log(level, "Linked areas 'link_source' <-> 'link_target' at spawn+30/50")
    }

    private void testPresetArea(ServerLevel level) {
        def preset = Common.getAreaManager().getPreset("area_test_preset")
        assert preset != null : "preset 'area_test_preset' should exist"

        def zone = preset.create(
                Identifier.fromNamespaceAndPath("testbundle", "preset_test"),
                new Vec3(70, 60, -5),
                new Vec3(80, 70, 5),
                level.dimension(),
                Color.VIOLET)
        Common.getAreaManager().register(level, zone)
        log(level, "Preset area 'preset_test' at spawn+70 (tick interval="
                + zone.getModuleData(MID_TICK).getInt("interval") + ")")

        // Verify preset data was applied
        int interval = zone.getModuleData(MID_TICK).getInt("interval").orElse(-1)
        assert interval == 60 : "preset should set tick interval to 60, got " + interval
    }

    // ── Helper ───────────────────────────────────────────────────────

    private static void log(ServerLevel level, String msg) {
        level.server.sendSystemMessage(
                Component.literal("§a[AreaTest] " + msg))
    }

    @Override
    void onUnload() {
    }
}
