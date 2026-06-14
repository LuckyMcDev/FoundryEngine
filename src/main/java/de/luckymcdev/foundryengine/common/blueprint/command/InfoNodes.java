package de.luckymcdev.foundryengine.common.blueprint.command;

import com.mojang.serialization.JsonOps;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.Entity;

public final class InfoNodes {

    private InfoNodes() {
    }

    public static void registerAll(BlueprintEngine engine) {
        engine.register(BuiltinNode.create("cmd.say", "Say", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.GSTRING, "Message");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var msg = ctx.resolvePinAs(n.inputPin("Message"), String.class, "");
                    src.getServer().getCommands().performPrefixedCommand(src, "say " + msg);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.me", "Me", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.GSTRING, "Action");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var action = ctx.resolvePinAs(n.inputPin("Action"), String.class, "");
                    src.getServer().getCommands().performPrefixedCommand(src, "me " + action);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.msg", "Message", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.GSTRING, "Message");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var msg = ctx.resolvePinAs(n.inputPin("Message"), String.class, "");
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    src.getServer().getCommands().performPrefixedCommand(src, "msg " + target.getUUID() + " " + msg);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.bossbar", "Bossbar", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.STRING, "ID");
                    node.input(BlueprintTypes.COMPONENT, "Name");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var id = ctx.resolvePinAs(n.inputPin("ID"), String.class, "");
                    var name = ctx.resolvePinAs(n.inputPin("Name"), net.minecraft.network.chat.Component.class, null);
                    if (name == null) { e.continueChain(n, g, ctx); return; }
                    var json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, name).getOrThrow().toString();
                    src.getServer().getCommands().performPrefixedCommand(src, "bossbar add " + id + " " + json);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.title", "Title", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.TITLE_TYPE, "Type", "title");
                    node.input(BlueprintTypes.COMPONENT, "Text");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var type = ctx.resolvePinAs(n.inputPin("Type"), String.class, "title");
                    var text = ctx.resolvePinAs(n.inputPin("Text"), net.minecraft.network.chat.Component.class, null);
                    if (target == null || text == null) { e.continueChain(n, g, ctx); return; }
                    var json = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow().toString();
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "title " + target.getUUID() + " " + type + " " + json);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.playsound", "Play Sound", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.SOUND_EVENT, "Sound");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.FLOAT, "Volume", 1f);
                    node.input(BlueprintTypes.FLOAT, "Pitch", 1f);
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var sound = ctx.resolvePinAs(n.inputPin("Sound"), net.minecraft.sounds.SoundEvent.class, null);
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var volume = ctx.resolvePinAs(n.inputPin("Volume"), Float.class, 1f);
                    var pitch = ctx.resolvePinAs(n.inputPin("Pitch"), Float.class, 1f);
                    if (sound == null || target == null) { e.continueChain(n, g, ctx); return; }
                    var soundId = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(sound);
                    src.getServer().getCommands().performPrefixedCommand(src,
                            "playsound " + soundId + " master " + target.getUUID() + " ~ ~ ~ " + volume + " " + pitch);
                    e.continueChain(n, g, ctx);
                }));

        engine.register(BuiltinNode.create("cmd.stopsound", "Stop Sound", "Commands/Info",
                node -> {
                    node.execInput("Exec");
                    node.input(BlueprintTypes.ENTITY, "Target");
                    node.input(BlueprintTypes.SOUND_SOURCE, "Source", "master");
                    node.input(BlueprintTypes.SOUND_EVENT, "Sound");
                    node.execOutput("Then");
                },
                (n, e, g, ctx) -> {
                    var src = ctx.commandSource();
                    if (src == null || src.getServer() == null) { e.continueChain(n, g, ctx); return; }
                    var target = ctx.resolvePinAs(n.inputPin("Target"), Entity.class, null);
                    var source = ctx.resolvePinAs(n.inputPin("Source"), String.class, "master");
                    var sound = ctx.resolvePinAs(n.inputPin("Sound"), net.minecraft.sounds.SoundEvent.class, null);
                    if (target == null) { e.continueChain(n, g, ctx); return; }
                    if (sound != null) {
                        var soundId = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(sound);
                        src.getServer().getCommands().performPrefixedCommand(src,
                                "stopsound " + target.getUUID() + " " + source + " " + soundId);
                    } else {
                        src.getServer().getCommands().performPrefixedCommand(src, "stopsound " + target.getUUID() + " " + source);
                    }
                    e.continueChain(n, g, ctx);
                }));
    }
}
