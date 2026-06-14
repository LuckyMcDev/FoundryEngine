package de.luckymcdev.foundryengine.common.blueprint.builder;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.builder.BuilderBase;
import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.api.builder.particle.ParticleBuilder;
import de.luckymcdev.foundryengine.api.builder.recipe.RecipeBuilder;
import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.wrapper.DataComponentWrapper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class BuilderNodes {

    private static final Logger LOGGER = LogUtils.getLogger();

    private BuilderNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        registerCreateItemBuilder(engine);
        registerItemStacksTo(engine);
        registerItemFireResistant(engine);
        registerItemComponent(engine);
        registerCreateBlockBuilder(engine);
        registerBlockNoItem(engine);
        registerCreateShapedRecipe(engine);
        registerCreateShapelessRecipe(engine);
        registerRecipeIngredient(engine);
        registerCreateSoundBuilder(engine);
        registerCreateParticleBuilder(engine);
        registerRegisterBuilder(engine);
    }

    // ======================== Item Builder ========================

    private static void registerCreateItemBuilder(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.create_item", "Create Item Builder", "Builder/Item",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "example_item");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "example_item");
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    ItemBuilder builder = ItemBuilder.create(id);
                    ctx.setVar("builder_" + n.id, builder);
                    n.setOutput("Builder", builder);
                    LOGGER.info("[Blueprint] Created item builder: {}", id);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerItemStacksTo(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.item_stacks_to", "Item Stacks To", "Builder/Item",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.input(BlueprintTypes.INT, "Count", 64);
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    int count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 64);
                    if (obj instanceof ItemBuilder ib) {
                        ib.stacksTo(count);
                        n.setOutput("Builder", obj);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerItemFireResistant(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.item_fire_resistant", "Item Fire Resistant", "Builder/Item",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    if (obj instanceof ItemBuilder ib) {
                        ib.fireResistant();
                        n.setOutput("Builder", obj);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    /**
     * Sets a DataComponent on the item builder using reflection.
     * Component name is looked up via DataComponentWrapper.resolve() from DataComponents class.
     * Value is passed as a string and coerced as needed.
     */
    private static void registerItemComponent(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.item_component", "Item Component", "Builder/Item",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.input(BlueprintTypes.STRING, "Component", "MAX_DAMAGE");
                    node.input(BlueprintTypes.OBJECT, "Value", 100);
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    String comp = ctx.resolvePinAs(n.inputPin("Component"), String.class, "");
                    Object value = ctx.resolvePin(n.inputPin("Value"));
                    if (obj instanceof ItemBuilder ib && !comp.isEmpty()) {
                        try {
                            var resolvedType = DataComponentWrapper.resolve(comp);
                            LOGGER.info("[Blueprint] Component {} resolved to type: {}", comp, resolvedType.getClass().getName());
                            ib.component(comp, value);
                            n.setOutput("Builder", obj);
                        } catch (Exception ex) {
                            LOGGER.error("[Blueprint] Failed to set component '{}': {}", comp, ex.getMessage());
                        }
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Block Builder ========================

    private static void registerCreateBlockBuilder(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.create_block", "Create Block Builder", "Builder/Block",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "example_block");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "example_block");
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    BlockBuilder builder = BlockBuilder.create(id);
                    ctx.setVar("builder_" + n.id, builder);
                    n.setOutput("Builder", builder);
                    LOGGER.info("[Blueprint] Created block builder: {}", id);
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerBlockNoItem(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.block_no_item", "Block No Item", "Builder/Block",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    if (obj instanceof BlockBuilder bb) {
                        bb.noItem();
                        n.setOutput("Builder", obj);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Recipe Builder ========================

    private static void registerCreateShapedRecipe(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.shaped_recipe", "Shaped Recipe", "Builder/Recipe",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "my_recipe");
                    node.input(BlueprintTypes.ITEM_STACK, "Result", ItemStack.EMPTY);
                    node.input(BlueprintTypes.INT, "Count", 1);
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "my_recipe");
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    ItemStack result = ctx.resolvePinAs(n.inputPin("Result"), ItemStack.class, ItemStack.EMPTY);
                    int count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 1);
                    if (!result.isEmpty()) {
                        RecipeBuilder builder = RecipeBuilder.shaped(id, result.getItem());
                        builder.count(count);
                        ctx.setVar("builder_" + n.id, builder);
                        n.setOutput("Builder", builder);
                        LOGGER.info("[Blueprint] Created shaped recipe: {}", id);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerCreateShapelessRecipe(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.shapeless_recipe", "Shapeless Recipe", "Builder/Recipe",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "my_recipe");
                    node.input(BlueprintTypes.ITEM_STACK, "Result", ItemStack.EMPTY);
                    node.input(BlueprintTypes.INT, "Count", 1);
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "my_recipe");
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    ItemStack result = ctx.resolvePinAs(n.inputPin("Result"), ItemStack.class, ItemStack.EMPTY);
                    int count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 1);
                    if (!result.isEmpty()) {
                        RecipeBuilder builder = RecipeBuilder.shapeless(id, result.getItem());
                        builder.count(count);
                        ctx.setVar("builder_" + n.id, builder);
                        n.setOutput("Builder", builder);
                        LOGGER.info("[Blueprint] Created shapeless recipe: {}", id);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    private static void registerRecipeIngredient(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.recipe_ingredient", "Recipe Ingredient", "Builder/Recipe",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.input(BlueprintTypes.ITEM_STACK, "Item", ItemStack.EMPTY);
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    ItemStack item = ctx.resolvePinAs(n.inputPin("Item"), ItemStack.class, ItemStack.EMPTY);
                    if (obj instanceof RecipeBuilder rb && !item.isEmpty()) {
                        rb.requires(item.getItem());
                        n.setOutput("Builder", obj);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Sound Builder ========================

    private static void registerCreateSoundBuilder(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.create_sound", "Create Sound Builder", "Builder/Sound",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "my_sound");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "my_sound");
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    SoundBuilder builder = SoundBuilder.create(id);
                    ctx.setVar("builder_" + n.id, builder);
                    n.setOutput("Builder", builder);
                    LOGGER.info("[Blueprint] Created sound builder: {}", id);
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Particle Builder ========================

    private static void registerCreateParticleBuilder(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.create_particle", "Create Particle Builder", "Builder/Particle",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "Namespace", "foundry");
                    node.input(BlueprintTypes.STRING, "Path", "my_particle");
                    node.input(BlueprintTypes.INT, "Lifetime", 20);
                    node.input(BlueprintTypes.OBJECT, "Color");
                    node.execOutput("Then");
                    node.output(BlueprintTypes.OBJECT, "Builder");
                },
                (n, e, g, ctx) -> {
                    String ns = ctx.resolvePinAs(n.inputPin("Namespace"), String.class, "foundry");
                    String path = ctx.resolvePinAs(n.inputPin("Path"), String.class, "my_particle");
                    int lifetime = ctx.resolvePinAs(n.inputPin("Lifetime"), Integer.class, 20);
                    Identifier id = Identifier.fromNamespaceAndPath(ns, path);
                    ParticleBuilder builder = ParticleBuilder.create(id);
                    builder.lifetime(lifetime);
                    Object colorObj = ctx.resolvePin(n.inputPin("Color"));
                    if (colorObj instanceof Color c) builder.color(c);
                    ctx.setVar("builder_" + n.id, builder);
                    n.setOutput("Builder", builder);
                    LOGGER.info("[Blueprint] Created particle builder: {}", id);
                    e.continueChain(n, g, ctx);
                }));
    }

    // ======================== Generic Register ========================

    /**
     * Generic register node that accepts ANY BuilderBase and queues it on the engine.
     * The engine's processPending*Registrations methods handle the actual registration
     * during mod setup.
     */
    private static void registerRegisterBuilder(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("builder.register", "Register Builder", "Builder",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.OBJECT, "Builder");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    Object obj = ctx.resolvePin(n.inputPin("Builder"));
                    if (obj instanceof BuilderBase<?> builder) {
                        e.addPendingBuilder(builder);
                        LOGGER.info("[Blueprint] Queued builder for registration: {} ({})",
                                builder.getId(), builder.getClass().getSimpleName());
                    } else {
                        LOGGER.warn("[Blueprint] Register: not a BuilderBase: {}",
                                obj != null ? obj.getClass().getName() : "null");
                    }
                    e.continueChain(n, g, ctx);
                }));
    }
}
