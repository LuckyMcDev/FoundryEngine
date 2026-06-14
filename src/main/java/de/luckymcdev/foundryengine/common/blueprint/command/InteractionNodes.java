package de.luckymcdev.foundryengine.common.blueprint.command;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class InteractionNodes {

    private InteractionNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.give", "Give Item", "Commands/Items",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.ITEM_STACK, "Item");
                    node.input(BlueprintTypes.INT, "Count", 1);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var item = ctx.resolvePinAs(n.inputPin("Item"), ItemStack.class, null);
                    var count = ctx.resolvePinAs(n.inputPin("Count"), Integer.class, 1);
                    if (target == null || item == null) { e.continueChain(n, g, ctx); return; }
                    var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem());
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "give " + target.getUUID() + " " + id + " " + count);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.clear", "Clear Inventory", "Commands/Items",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "clear " + target.getUUID());
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.enchant", "Enchant", "Commands/Items",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.ENCHANTMENT, "Enchantment");
                    node.input(BlueprintTypes.INT, "Level", 1);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var ench = ctx.resolvePinAs(n.inputPin("Enchantment"), Holder.class, null);
                    var id = src.getLevel().registryAccess()
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .getKey((Enchantment) ench.value());
                    var level = ctx.resolvePinAs(n.inputPin("Level"), Integer.class, 1);
                    if (target == null || ench == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "enchant " + target.getScoreboardName() + " " + id + " " + level);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.effect", "Effect", "Commands/Items",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.EFFECT, "Effect");
                    node.input(BlueprintTypes.INT, "Duration (s)", 30);
                    node.input(BlueprintTypes.INT, "Amplifier", 0);
                    node.input(BlueprintTypes.BOOL, "Show Particles", true);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var effect = ctx.resolvePinAs(n.inputPin("Effect"), net.minecraft.world.effect.MobEffect.class, null);
                    var duration = ctx.resolvePinAs(n.inputPin("Duration (s)"), Integer.class, 30);
                    var amplifier = ctx.resolvePinAs(n.inputPin("Amplifier"), Integer.class, 0);
                    var particles = ctx.resolvePinAs(n.inputPin("Show Particles"), Boolean.class, true);
                    if (target == null || effect == null) { e.continueChain(n, g, ctx); return; }
                    var id = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "effect give " + target.getUUID() + " " + id + " " + duration + " " + amplifier);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.experience", "Experience", "Commands/Items",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.INT, "Amount", 1);
                    node.input(BlueprintTypes.STRING, "Type", "points");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var amount = ctx.resolvePinAs(n.inputPin("Amount"), Integer.class, 1);
                    var type = ctx.resolvePinAs(n.inputPin("Type"), String.class, "points");
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "experience " + ("add ") + target.getUUID() + " " + amount + " " + type);
                    e.continueChain(n, g, ctx);
                }));
    }
}
