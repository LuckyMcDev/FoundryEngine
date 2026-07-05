# Examples

This page contains complete working examples organized by category. All examples use the Groovy bundle scripting API.

## Items

### Custom Item with Properties

```groovy
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Rarity

ItemBuilder.create(id("my_item"))
    .fireResistant()
    .stacksTo(16)
    .use { level, player, hand ->
        player.sendSystemMessage(Component.literal("Used!"))
        InteractionResult.SUCCESS
    }

BundleEvents.registry {
    it.items(myItem)
}
```

### Item with Callbacks

```groovy
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder

ItemBuilder.create(id("wand"))
    .use { level, player, hand ->
        player.sendSystemMessage(Component.literal("Whoosh!"))
        return InteractionResult.SUCCESS
    }
    .useOn { context ->
        println "Used on ${context.pos}"
        return InteractionResult.SUCCESS
    }
    .inventoryTick { stack, level, owner, slot ->
        if (level.gameTime % 20 == 0) {
            // Per-second effect while in inventory
        }
    }
    .hurtEnemy { stack, mob, attacker ->
        mob.setSecondsOnFire(3)
    }
```

### Food Item

```groovy
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.component.Consumables
import net.minecraft.world.food.FoodProperties

ItemBuilder.create(id("cosmic_apple"))
    .component(DataComponents.RARITY, Rarity.EPIC)
    .component(DataComponents.FOOD, new FoodProperties.Builder()
        .nutrition(4).saturationModifier(0.3f).alwaysEdible().build())
    .component(DataComponents.CONSUMABLE, Consumables.defaultFood().build())
    .stacksTo(16)
```

## Blocks

### Block with Callbacks

```groovy
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder
import de.luckymcdev.foundryengine.common.event.BundleEvents

BlockBuilder.create(id("magic_block"))
    .properties { it.strength(2.0f, 3.0f).lightLevel { 15 } }
    .stepOn { level, pos, onState, entity ->
        entity.setSecondsOnFire(3)
    }
    .destroy { level, pos, state ->
        println "Block destroyed at $pos"
    }
    .setPlacedBy { level, pos, state, by, itemStack ->
        println "Placed by ${by?.name}"
    }

BundleEvents.registry {
    it.blocks(magicBlock)
}
```

### Block with Custom Properties

```groovy
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder

// Block with no collision and no item
BlockBuilder.create(id("invisible_wall"))
    .noItem()
    .properties { it.noCollision().strength(-1.0f, 3600000.0f) }
    .generateData(false)

// Block with custom factory (e.g. SlabBlock)
BlockBuilder.create(id("my_slab"))
    .factory { new SlabBlock(it) }
    .properties { it.noOcclusion().lightLevel { 15 } }

// Block with step-on callback (damage)
BlockBuilder.create(id("hot_plate"))
    .stepOn { level, pos, onState, entity ->
        if (entity instanceof LivingEntity) {
            entity.hurt(entity.damageSources().hotFloor(), 1.0f)
        }
    }
```

## Recipes

### Shaped Recipe

```groovy
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder

RecipeBuilder.shaped(id("test_shaped"), Items.DIAMOND_SWORD)
    .pattern(" D ", " D ", " S ")
    .define('D' as char, Items.DIAMOND)
    .define('S' as char, Items.STICK)
    .category(RecipeCategory.COMBAT)
    .unlockedBy("has_diamond",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
```

### Shapeless Recipe

```groovy
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder

RecipeBuilder.shapeless(id("test_shapeless"), Items.FLINT_AND_STEEL)
    .requires(Items.IRON_INGOT)
    .requires(Items.FLINT)
    .unlockedBy("has_iron",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
```

### Smelting Recipe

```groovy
import de.luckymcdev.foundryengine.common.builder.recipe.RecipeBuilder

RecipeBuilder.smelting(id("test_smelting"), Items.IRON_INGOT)
    .ingredient(Items.IRON_ORE)
    .experience(0.7f)
    .cookingTime(200)
    .unlockedBy("has_ore",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_ORE))
```

### Blasting

```groovy
RecipeBuilder.blasting(id("test_blasting"), Items.IRON_INGOT)
    .ingredient(Items.RAW_IRON)
    .experience(0.7f)
    .cookingTime(100)
```

### Smoking

```groovy
RecipeBuilder.smoking(id("test_smoking"), Items.COOKED_BEEF)
    .ingredient(Items.BEEF)
    .experience(0.35f)
    .cookingTime(100)
```

### Campfire Cooking

```groovy
RecipeBuilder.campfireCooking(id("test_campfire"), Items.COOKED_BEEF)
    .ingredient(Items.BEEF)
    .experience(0.35f)
    .cookingTime(600)
```

### Stonecutting

```groovy
RecipeBuilder.stonecutting(id("test_stonecutting"), Items.STONE_SLAB)
    .ingredient(Items.STONE)
    .count(2)
```

### Smithing Transform

```groovy
RecipeBuilder.smithingTransform(id("test_smithing"), Items.NETHERITE_SWORD)
    .base(Items.DIAMOND_SWORD)
    .addition(Items.NETHERITE_INGOT)
    .template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
    .unlockedBy("has_netherite",
        InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_INGOT))
```

## Events

### Responding to Block Broken

```groovy
import de.luckymcdev.foundryengine.common.event.BlockEvents

BlockEvents.broken {
    println "${it.player.name} broke a block at ${it.pos}"
}
```

### Player Tick

```groovy
import de.luckymcdev.foundryengine.common.event.PlayerEvents

PlayerEvents.tick {
    if (it.player.tickCount % 20 == 0) {
        // Every second
        def player = it.player
        if (player.isShiftKeyDown()) {
            println "${player.name} is sneaking!"
        }
    }
}
```

### Server Started

```groovy
import de.luckymcdev.foundryengine.common.event.ServerEvents

ServerEvents.started {
    println "Server is ready!"
}
```

### Entity Death

```groovy
import de.luckymcdev.foundryengine.common.event.EntityEvents

EntityEvents.death {
    println "${it.entity} died"
}

EntityEvents.drops {
    // Modify mob drops
}
```

### Custom Event

```groovy
import de.luckymcdev.foundryengine.common.event.BundleEvents
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent

BundleEvents.custom(LivingDeathEvent.class, {
    println "${it.entity} died"
})
```

### Block Modification at Runtime

```groovy
import de.luckymcdev.foundryengine.common.event.BlockEvents

BlockEvents.modification {
    it.hasCollision(false)
      .explosionResistance(1000.0f)
      .lightEmission(15)
      .soundType(SoundType.AMETHYST)
      .friction(0.1f)
      .speedFactor(0.5f)
      .jumpFactor(2.0f)
      .randomlyTicking(true)
}
```

## Areas

### Healing Zone

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.module.AreaTickModule

Common.getAreaManager().registerModuleType(new AreaTickModule() {
    @Override Identifier id() { return Common.id("heal_zone") }
    @Override void tick(ServerLevel level, Area area) {
        level.getEntities().getAll().each { e ->
            if (e instanceof LivingEntity && area.contains(e.position())) {
                e.heal(0.5f)
            }
        }
    }
})
```

### Welcome Message on Enter

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule

Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override Identifier id() { return Common.id("welcome") }
    @Override void onEnter(ServerPlayer player, Area area) {
        player.sendSystemMessage(
            Component.literal("Welcome to ${area.id()}!"))
    }
})
```

### Linked Areas (Portal)

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.area.module.AreaEnterModule

Common.getAreaManager().registerModuleType(new AreaEnterModule() {
    @Override Identifier id() { return Common.id("portal") }
    @Override void onEnter(ServerPlayer player, Area area) {
        def target = Common.getAreaManager()
            .find(area.getLevel(), Common.id("linked_area"))
        if (target != null) {
            def center = target.getBounds().getCenter()
            player.teleportTo(serverLevel, center.x, center.y, center.z,
                Set.of(), player.yRot, player.xRot)
        }
    }
})
```

## Cutscenes

### Creating and Playing a Cutscene

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene
import de.luckymcdev.foundryengine.common.easing.BezierPath
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.Vec2

def path = new BezierPath(new Vec3(0, 64, 0))

def cutscene = new Cutscene("intro",
    new Vec2(0, 0), new Vec2(-10, 90),
    path)
cutscene.setDefaultLength(100)

Common.getCutsceneManager().add(serverLevel.dimension(), cutscene)

// Play it via server command:
player.server.commands.performCommand(
    player.server.createCommandSourceStack(),
    "engine cutscene play ${player.name.string} intro 100 SINE_IN_OUT 10 10"
)
```

### Cutscene with Attachments

```groovy
import de.luckymcdev.foundryengine.common.cutscene.model.EffectAttachment
import de.luckymcdev.foundryengine.common.cutscene.model.CommandAttachment

// Fade to black at 30% through
cutscene.addAttachment(new EffectAttachment(
    0.3f, "black", 10, 20, 10, "SINE_IN_OUT"))

// Run a command at 50%
cutscene.addAttachment(new CommandAttachment(
    0.5f, "say Hello from the cutscene!", 0))
```

## Particles

### Simple Particle

```groovy
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.client.particle.ParticleLayer
import de.luckymcdev.foundryengine.common.easing.Easing

ParticleBuilder.create(Common.id("sparkle"))
    .alwaysShow()
    .lifetime(30)
    .layer(ParticleLayer.TRANSLUCENT)
    .color(Color.WHITE, Color.RED, Easing.SINE_IN)
    .scale(0.5f, 1.5f, Easing.SINE_OUT)
    .velocity(new Vector3d(0.0, 0.1, 0.0))

BundleEvents.registry {
    it.particles(sparkle)
}
```

### Keyframe-Driven Particle

```groovy
import de.luckymcdev.foundryengine.common.builder.particle.ParticleBuilder
import de.luckymcdev.foundryengine.client.particle.ParticleLayer
import de.luckymcdev.foundryengine.client.particle.data.*
import de.luckymcdev.foundryengine.common.easing.Easing

def complexFx = ParticleBuilder.create(Common.id("complex_fx"))
    .lifetime(100)
    .layer(ParticleLayer.TRANSLUCENT)
    .colorData(new ParticleColorData(new KeyframeSequence<Color>()
        .add(Color.RED, 0f, Easing.LINEAR)
        .add(Color.YELLOW, 0.5f, Easing.SINE_IN_OUT)
        .add(Color.BLUE, 1f, Easing.LINEAR)))
    .scaleData(new ParticleScaleData(new KeyframeSequence<Float>()
        .add(0.5f, 0f, Easing.LINEAR)
        .add(2.0f, 0.5f, Easing.CUBIC_OUT)
        .add(0.0f, 1f, Easing.LINEAR)))
    .velocityData(new ParticleVelocityData(new KeyframeSequence<Vector3d>()
        .add(new Vector3d(0, 0.2, 0), 0f, Easing.LINEAR)
        .add(new Vector3d(0, 0, 0), 1f, Easing.SINE_OUT)))
```

## Stages

### Gate Items by Stage

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.EntityEvents

def stages = Common.getGameStageHandler()

// Gate an item
stages.item().requireStages(Items.ELYTRA,
    Component.literal("Defeat the Ender Dragon first!"),
    "dragon_slayer")

// Gate a mob
stages.mobs().requireStages(EntityType.WITHER, "nether_complete")

// Gate a dimension
stages.dimensions().requireStages(
    ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.parse("minecraft:the_end")),
    "end_open")
```

### Grant Stage on Boss Kill

```groovy
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.EntityEvents

def stages = Common.getGameStageHandler()

EntityEvents.death {
    if (it.entity.getType() == EntityType.ENDER_DRAGON) {
        def player = it.source.getEntity()
        if (player instanceof ServerPlayer) {
            stages.addStage(player, "dragon_slayer")
        }
    }
}

// Check stage
PlayerEvents.tick {
    if (stages.hasStage(it.player, "dragon_slayer")) {
        // Grant special powers
    }
}
```

## Worlds

### Temporary Runtime Dimension

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

### Persistent Runtime Dimension

```groovy
import de.luckymcdev.foundryengine.common.world.level.EngineLevels

def persistent = EngineLevels.get(server).getOrOpenPersistentLevel(
    Common.id("my_world"), config)
```

## Complete Bundle Entrypoint

```groovy
package mybundle

import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import de.luckymcdev.foundryengine.common.Common
import de.luckymcdev.foundryengine.common.event.BundleEvents
import de.luckymcdev.foundryengine.common.event.ServerEvents
import de.luckymcdev.foundryengine.common.event.PlayerEvents
import de.luckymcdev.foundryengine.common.event.BlockEvents
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder
import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder
class Entrypoint implements BundleEntrypoint {

    @Override
    void onLoad() {
        // --- Define Items ---
        def myItem = ItemBuilder.create(Common.id("custom_item"))
            .fireResistant().stacksTo(16)
            .use { level, player, hand ->
                player.sendSystemMessage(
                    Component.literal("Custom item used!"))
                InteractionResult.SUCCESS
            }

        // --- Define Blocks ---
        def myBlock = BlockBuilder.create(Common.id("custom_block"))
            .properties { it.strength(2.0f, 3.0f).lightLevel { 15 } }
            .stepOn { level, pos, state, entity ->
                entity.setSecondsOnFire(3)
            }

        // --- Register Everything ---
        BundleEvents.registry {
            it.items(myItem)
            it.blocks(myBlock)
        }

        // --- Listen to Events ---
        ServerEvents.started {
            println "Bundle loaded on server!"
        }

        PlayerEvents.tick {
            if (it.player.tickCount % 100 == 0) {
                it.player.sendSystemMessage(
                    Component.literal("Still alive!"))
            }
        }

        BlockEvents.broken {
            println "${it.player.name} broke " +
                "${it.state.block} at ${it.pos}"
        }
    }

    @Override
    void onUnload() {
        println "Bundle unloaded!"
    }
}
```

## See Also

- The `ExampleBundles/testbundle` directory in the repository for a complete working bundle with items, blocks, all 9 recipe types, sounds, commands, and event listeners.
- [Concepts Overview](../concepts/core/) for detailed documentation on every feature.
- [Builders](../concepts/core/builders) — Full builder API reference
- [Events](../concepts/core/events) — Full event API reference
