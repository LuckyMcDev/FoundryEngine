package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Transformation;
import de.luckymcdev.foundryengine.common.world.entity.*;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class TestCommand implements EngineCommand {

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
        return Commands.literal("test")
                .requires(this::isAdmin)
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();

                    killExisting(level);

                    EntitySpawner.spawnServer(level, EngineEntities.BLOCK_DISPLAY.get(),
                            new Vec3(-3, 100, 0), display -> {

                                display.setDisplayBlockState(Blocks.STONE.defaultBlockState());

                                display.setDisplayTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf()
                                ));

                                display.setInteractionConsumer((d, player, hand, location) -> {
                                    if (hand == InteractionHand.MAIN_HAND) {
                                        player.sendSystemMessage(Component.literal("[BlockDisplay] MainHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    if (hand == InteractionHand.OFF_HAND) {
                                        player.sendSystemMessage(Component.literal("[BlockDisplay] OffHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    return InteractionResult.PASS;
                                });

                                display.setAttackConsumer((d, player) ->
                                        player.sendSystemMessage(Component.literal("[BlockDisplay] Attack!")));
                            });

                    EntitySpawner.spawnServer(level, EngineEntities.ITEM_DISPLAY.get(),
                            new Vec3(0, 100, 0), display -> {

                                display.setDisplayItemStack(Items.DIAMOND_SWORD.getDefaultInstance());

                                display.setDisplayTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf()
                                ));

                                display.setInteractionConsumer((d, player, hand, location) -> {
                                    if (hand == InteractionHand.MAIN_HAND) {
                                        player.sendSystemMessage(Component.literal("[ItemDisplay] MainHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    if (hand == InteractionHand.OFF_HAND) {
                                        player.sendSystemMessage(Component.literal("[ItemDisplay] OffHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    return InteractionResult.PASS;
                                });

                                display.setAttackConsumer((d, player) ->
                                        player.sendSystemMessage(Component.literal("[ItemDisplay] Attack!")));
                            });

                    EntitySpawner.spawnServer(level, EngineEntities.TEXT_DISPLAY.get(),
                            new Vec3(3, 100, 0), display -> {

                                display.setDisplayText(Component.literal("Hello World!"));

                                display.setDisplayTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf()
                                ));

                                display.setInteractionConsumer((d, player, hand, location) -> {
                                    if (hand == InteractionHand.MAIN_HAND) {
                                        player.sendSystemMessage(Component.literal("[TextDisplay] MainHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    if (hand == InteractionHand.OFF_HAND) {
                                        player.sendSystemMessage(Component.literal("[TextDisplay] OffHand!"));
                                        return InteractionResult.SUCCESS;
                                    }
                                    return InteractionResult.PASS;
                                });

                                display.setAttackConsumer((d, player) ->
                                        player.sendSystemMessage(Component.literal("[TextDisplay] Attack!")));
                            });

                    context.getSource().sendSuccess(
                            () -> Component.literal("Spawned 3 engine display entities (block / item / text)."), false);
                    return 1;
                });
    }
}