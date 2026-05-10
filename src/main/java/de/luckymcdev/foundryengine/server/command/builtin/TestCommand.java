package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Transformation;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.world.entity.EngineEntities;
import de.luckymcdev.foundryengine.common.world.entity.EntitySpawner;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineBlockDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineItemDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineTextDisplay;
import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelConfig;
import de.luckymcdev.foundryengine.common.world.level.runtime.RuntimeLevelHandle;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCommand implements EngineCommand {
    private final HashMap<Identifier, RuntimeLevelHandle> worlds = new HashMap<>();

    private static void killExisting(ServerLevel level) {
        List<Entity> toRemove = new ArrayList<>();
        level.getEntities().getAll().forEach(e -> {
            if (e instanceof EngineBlockDisplay || e instanceof EngineItemDisplay || e instanceof EngineTextDisplay) {
                toRemove.add(e);
            }
        });
        toRemove.forEach(Entity::discard);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        var testRoot = Commands.literal("test")
                .requires(this::isAdmin)
                // Original /engine test command – spawns display entities
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();

                    killExisting(level);

                    EntitySpawner.spawnServer(level, EngineEntities.BLOCK_DISPLAY.get(),
                            new Vec3(-5, 100, 0), display -> {

                                display.setBlockState(Blocks.STONE.defaultBlockState());
                                display.setTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf().rotateX((float) Math.toRadians(45))
                                ));
                                display.setInteractionCommand(
                                        "engine eval tellPlayer(\"Hello!\")"
                                );
                                display.setOffhandInteractionCommand("say [BlockDisplay] Offhand clicked!");
                                display.setAttackCommand("say [BlockDisplay] attacked!");
                            });

                    EntitySpawner.spawnServer(level, EngineEntities.ITEM_DISPLAY.get(),
                            new Vec3(0, 100, 0), display -> {
                                display.setPickable(true);
                                display.setGlowColorOverride(Color.ORANGE.argb());
                                display.setItemStack(Items.DIAMOND_SWORD.getDefaultInstance());
                                display.setTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf()
                                ));

                                display.setInteractionCommand("say [ItemDisplay] Mainhand clicked!");
                                display.setOffhandInteractionCommand("say [ItemDisplay] Offhand clicked!");
                                display.setAttackCommand("say [ItemDisplay] attacked!");
                            });

                    EntitySpawner.spawnServer(level, EngineEntities.TEXT_DISPLAY.get(),
                            new Vec3(3, 100, 0), display -> {

                                display.setText(Component.literal("Hello World!"));
                                display.setAlignment(Display.TextDisplay.Align.CENTER);
                                display.setTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf()
                                ));

                                display.setInteractionCommand("say [TextDisplay] Mainhand clicked!");
                                display.setOffhandInteractionCommand("say [TextDisplay] Offhand clicked!");
                                display.setAttackCommand("say [TextDisplay] attacked!");
                            });

                    context.getSource().sendSuccess(
                            () -> Component.literal("Spawned 3 engine display entities (block / item / text)."), false);

                    context.getSource().sendSuccess(() -> ChatIcons.ENERGY, false);
                    return 1;
                });

        // -----------------------------------------------------------------------
        // /engine test world open <name> <temp>
        // -----------------------------------------------------------------------
        testRoot.then(Commands.literal("world")
                .then(Commands.literal("open")
                        .then(Commands.argument("name", IdentifierArgument.id())
                                .then(Commands.argument("temp", BoolArgumentType.bool())
                                        .executes(context -> {
                                            var source = context.getSource();
                                            try {
                                                var temp = BoolArgumentType.getBool(context, "temp");
                                                final long[] ref = {System.currentTimeMillis()};

                                                var id = IdentifierArgument.getId(context, "name");
                                                var server = source.getServer();
                                                var config = new RuntimeLevelConfig()
                                                        .setGenerator(server.overworld().getChunkSource().getGenerator())
                                                        .setGameRule(GameRules.BLOCK_DROPS, false)
                                                        .setSeed(id.hashCode());

                                                RuntimeLevelHandle handle;
                                                if (temp) {
                                                    handle = EngineLevels.get(server).openTemporaryLevel(id, config);
                                                } else {
                                                    handle = EngineLevels.get(server).getOrOpenPersistentLevel(id, config);
                                                }

                                                source.sendSuccess(
                                                        () -> Component.literal("LevelCreate: " + (System.currentTimeMillis() - ref[0])),
                                                        false);

                                                worlds.put(id, handle);

                                                ref[0] = System.currentTimeMillis();
                                                if (source.getEntity() != null) {
                                                    source.getEntity().teleport(
                                                            new TeleportTransition(
                                                                    handle.asLevel(),
                                                                    new Vec3(0, 100, 0),
                                                                    Vec3.ZERO,
                                                                    0, 0,
                                                                    TeleportTransition.DO_NOTHING));
                                                }

                                                source.sendSuccess(
                                                        () -> Component.literal("Teleport: " + (System.currentTimeMillis() - ref[0])),
                                                        false);

                                                return 1;
                                            } catch (Throwable e) {
                                                source.sendFailure(Component.literal("Failed to open world"));
                                                return 0;
                                            }
                                        }))
                        )
                )
                // -----------------------------------------------------------------------
                // /engine test world delete <name>
                // -----------------------------------------------------------------------
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", IdentifierArgument.id())
                                .executes(context -> {
                                    var source = context.getSource();
                                    try {
                                        var id = IdentifierArgument.getId(context, "name");
                                        var handle = worlds.get(id);
                                        if (handle == null) {
                                            source.sendFailure(Component.literal("This world does not exist"));
                                            return 0;
                                        }
                                        handle.delete();
                                        worlds.remove(id);
                                        source.sendSuccess(
                                                () -> Component.literal("World \"" + id + "\" deleted"),
                                                true);
                                    } catch (Throwable e) {
                                        source.sendFailure(Component.literal("Failed to delete world"));
                                    }
                                    return 1;
                                })
                        )
                )
                // -----------------------------------------------------------------------
                // /engine test world unload <name>
                // -----------------------------------------------------------------------
                .then(Commands.literal("unload")
                        .then(Commands.argument("name", IdentifierArgument.id())
                                .executes(context -> {
                                    var source = context.getSource();
                                    try {
                                        var id = IdentifierArgument.getId(context, "name");
                                        RuntimeLevelHandle handle = worlds.get(id);
                                        if (handle == null) {
                                            source.sendFailure(Component.literal("This world does not exist"));
                                            return 0;
                                        }
                                        handle.unload();
                                        worlds.remove(id);
                                        source.sendSuccess(
                                                () -> Component.literal("World \"" + id + "\" unloaded"),
                                                true);
                                    } catch (Throwable e) {
                                        source.sendFailure(Component.literal("Failed to unload world"));
                                    }
                                    return 1;
                                })
                        )
                )
        );

        return testRoot;
    }
}