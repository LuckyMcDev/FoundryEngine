# Examples

## Creating a Custom Item

```groovy
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.api.event.BundleEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Rarity

// A simple fire-resistant item
ItemBuilder.create(id("my_item"))
    .fireResistant()
    .stacksTo(16)
    .use { level, player, hand ->
        player.sendSystemMessage(Component.literal("Used!"))
        InteractionResult.SUCCESS
    }

// Register in onLoad
BundleEvents.registry {
    it.items(myItem)
}
```

## Creating a Block with Callbacks

```groovy
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.api.event.BundleEvents

BlockBuilder.create(id("magic_block"))
    .properties { it.strength(2.0f, 3.0f).lightLevel { 15 } }
    .stepOn { level, pos, onState, entity ->
        entity.setSecondsOnFire(3)
    }

BundleEvents.registry {
    it.blocks(magicBlock)
}
```

## Responding to Events

```groovy
import de.luckymcdev.foundryengine.api.event.BlockEvents
import de.luckymcdev.foundryengine.api.event.PlayerEvents
import de.luckymcdev.foundryengine.api.event.ServerEvents

PlayerEvents.tick {
    if (it.player.tickCount % 20 == 0) {
        // Every second
    }
}

BlockEvents.broken {
    println "${it.player.name} broke a block at ${it.pos}"
}

ServerEvents.started {
    println "Server is ready!"
}
```

## Using Areas

```groovy
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule
import de.luckymcdev.foundryengine.common.area.module.AreaLeaveModule

Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override Identifier id() { return Common.id("log_enter") }
    @Override void onEnter(ServerPlayer player, Area area) {
        println "Player ${player.name} entered area: ${area.id()}"
    }
})

Common.getAreaManager().registerModuleType(new AreaLeaveModule() {
    @Override Identifier id() { return Common.id("log_leave") }
    @Override void onLeave(ServerPlayer player, Area area) {
        println "Player ${player.name} left area: ${area.id()}"
    }
})
```

## Creating a Runtime Dimension

```groovy
import de.luckymcdev.foundryengine.common.world.level.EngineLevels
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig
import de.luckymcdev.foundryengine.common.world.level.util.VoidChunkGenerator

def server = MinecraftServer.getServer()
def config = new RuntimeLevelConfig()
    .setGenerator(new VoidChunkGenerator(server, "minecraft:plains"))
    .setShouldTickTime(true)
    .setGameRule(GameRules.DO_MOB_SPAWNING, false)

def handle = EngineLevels.get(server).openTemporaryLevel(
    Common.id("my_dungeon"), config)

player.teleport(new TeleportTransition(
    handle.asLevel(), new Vec3(0, 100, 0), Vec3.ZERO, 0, 0,
    TeleportTransition.DO_NOTHING))
```

## Working with Game Stages

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.api.event.EntityEvents

def stages = Common.getGameStageHandler()

// Grant stage on boss kill
EntityEvents.death {
    if (it.entity.getType() == EntityType.ENDER_DRAGON) {
        def player = it.source.getEntity()
        if (player instanceof ServerPlayer) {
            stages.addStage(player, "dragon_slayer")
        }
    }
}

// Gate an item
stages.item().requireStages(Items.ELYTRA,
    Component.literal("Defeat the Ender Dragon first!"),
    "dragon_slayer")
```

## Registering a Shaped Recipe

```groovy
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder

RecipeBuilder.shaped(id("test_shaped"), Items.DIAMOND_SWORD)
    .pattern(" D ", " D ", " S ")
    .define('D' as char, Items.DIAMOND)
    .define('S' as char, Items.STICK)
    .category(RecipeCategory.COMBAT)
    .unlockedBy("has_diamond",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))

BundleEvents.registry {
    it.recipes(testShaped)
}
```

## Using Keyframed Particles

```groovy
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.api.builder.particle.ParticleLayer
import de.luckymcdev.foundryengine.client.particle.data.*
import de.luckymcdev.foundryengine.common.easing.Easing

def sparkle = ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()
    .lifetime(40)
    .layer(ParticleLayer.TRANSLUCENT)
    .colorData(new ParticleColorData(new KeyframeSequence<Color>()
        .add(Color.WHITE, 0f, Easing.LINEAR)
        .add(Color.GOLD, 0.3f, Easing.SINE_IN_OUT)
        .add(Color.ORANGE, 0.6f, Easing.SINE_IN_OUT)
        .add(Color.RED, 1f, Easing.LINEAR)))
    .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
        .add(0.2f, 0f, Easing.LINEAR)
        .add(1.5f, 0.5f, Easing.CUBIC_OUT)
        .add(0f, 1f, Easing.LINEAR)))

BundleEvents.registry {
    it.particles(sparkle)
}
```

## Creating a Cutscene

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene
import de.luckymcdev.foundryengine.common.cutscene.model.bezier.BezierPath
import de.luckymcdev.foundryengine.common.cutscene.model.bezier.BezierPoint
import de.luckymcdev.foundryengine.common.Common

def points = [
    new BezierPoint(new Vector3d(0, 64, 0),
                    new Vector3d(0, 64, 0)),
    new BezierPoint(new Vector3d(10, 70, 10),
                    new Vector3d(10, 70, 10))
]
def cutscene = new Cutscene("intro",
    new Vector2d(0, 0), new Vector2d(-10, 90),
    new BezierPath(points))
cutscene.setDefaultLength(100)

Common.getCutsceneManager().add(serverLevel, cutscene)
```

## Full Bundle Entrypoint

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.api.event.BundleEvents
import de.luckymcdev.foundryengine.api.event.ServerEvents
import de.luckymcdev.foundryengine.api.event.PlayerEvents
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder

class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        // Define content
        def myItem = ItemBuilder.create(Common.id("custom_item"))
            .fireResistant().stacksTo(16)

        def myBlock = BlockBuilder.create(Common.id("custom_block"))
            .properties { it.strength(2.0f, 3.0f) }

        // Register
        BundleEvents.registry {
            it.items(myItem)
            it.blocks(myBlock)
        }

        // Listen to events
        ServerEvents.started {
            println "Bundle loaded on server!"
        }

        PlayerEvents.tick {
            if (it.player.tickCount % 100 == 0) {
                it.player.sendSystemMessage(
                    Component.literal("Still alive!"))
            }
        }
    }

    @Override
    void onUnload() {
        println "Bundle unloaded!"
    }
}
```

## See Also

- The `ExampleBundles/testbundle` directory in the repository for a complete working bundle with items, blocks, recipes (all 9 types), sounds, commands, and event listeners.
- [Concepts Overview](concepts/index) for detailed documentation on every feature.
