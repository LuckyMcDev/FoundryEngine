package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.math.Transformation;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.world.entity.EngineEntities;
import de.luckymcdev.foundryengine.common.world.entity.EntitySpawner;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineBlockDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineItemDisplay;
import de.luckymcdev.foundryengine.common.world.entity.display.EngineTextDisplay;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
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
                            new Vec3(-5, 100, 0), display -> {

                                display.setBlockState(Blocks.STONE.defaultBlockState());
                                display.setTransformation(new Transformation(
                                        new Vector3f(0f, 0.5f, 0f),
                                        new Quaternionf().rotateY((float) Math.toRadians(45)),
                                        new Vector3f(2f, 2f, 2f),
                                        new Quaternionf().rotateX((float) Math.toRadians(45))
                                ));

                                display.setInteractionCommand("say [BlockDisplay] Mainhand clicked!");
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
                    return 1;
                });
    }
}