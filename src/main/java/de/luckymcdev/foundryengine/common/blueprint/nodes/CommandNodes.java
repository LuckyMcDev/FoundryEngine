package de.luckymcdev.foundryengine.common.blueprint.nodes;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.CommandContext;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.*;

public final class CommandNodes {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CTX_KEY = "_cmd_ctx";
    private static final String CB = BlueprintEngine.Categories.COMMANDS_BASIC;
    private static final String CE = BlueprintEngine.Categories.COMMANDS_ENTITY;
    private static final String CW = BlueprintEngine.Categories.COMMANDS_WORLD;
    private static final String CP = BlueprintEngine.Categories.COMMANDS_PLAYER;
    private static final String CD = BlueprintEngine.Categories.COMMANDS_DATA;
    private static final String CT = BlueprintEngine.Categories.COMMANDS_TIME;
    private static final String CM = BlueprintEngine.Categories.COMMANDS_MISC;

    private CommandNodes() {
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private static void runCmd(BlueprintNode node, BlueprintEngine eng, BlueprintGraph graph, BlueprintContext bpc, String cmd) {
        MinecraftServer server = findServer(bpc);
        if (server == null) {
            LOGGER.warn("[Blueprint] Cannot run command: no server available");
            eng.executePin(node, "Continue", graph, bpc);
            return;
        }
        CommandSourceStack fallback = server.createCommandSourceStack();
        CommandContext cmdCtx = bpc.getVar(CTX_KEY, CommandContext.class);
        if (cmdCtx != null) fallback = cmdCtx.resolveSource(fallback);
        server.getCommands().performPrefixedCommand(fallback, cmd);
        eng.executePin(node, "Continue", graph, bpc);
    }

    private static MinecraftServer findServer(BlueprintContext bpc) {
        MinecraftServer s = bpc.getVar("Server", MinecraftServer.class);
        if (s != null) return s;
        Player p = bpc.getVar("Player", Player.class);
        if (p != null && p.level() instanceof ServerLevel sl) return sl.getServer();
        Entity e = bpc.getVar("Entity", Entity.class);
        if (e != null && e.level() instanceof ServerLevel sl) return sl.getServer();
        if (bpc.getVar("Level", Object.class) instanceof ServerLevel sl) return sl.getServer();
        return null;
    }

    private static String resolveSelector(BlueprintNode node, BlueprintContext bpc, String label) {
        Object raw = bpc.resolvePin(node.inputPin(label));
        if (raw instanceof Entity entity) return entity.getUUID().toString();
        if (raw instanceof String s) return s;
        return "@p";
    }

    private static String formatVec(Vec3 v) {
        return v.x + " " + v.y + " " + v.z;
    }

    private static String formatBlockPos(Vec3 v) {
        return (int) Math.floor(v.x) + " " + (int) Math.floor(v.y) + " " + (int) Math.floor(v.z);
    }

    private static Vec3 resolveVec3(BlueprintNode node, BlueprintContext c, String label, Vec3 fallback) {
        return c.resolvePinAs(node.inputPin(label), Vec3.class, fallback);
    }

    private static NodeBuilder basic(String id, String name) {
        return NodeBuilder.create(id, name, CB);
    }

    private static NodeBuilder entity(String id, String name) {
        return NodeBuilder.create(id, name, CE);
    }

    private static NodeBuilder world(String id, String name) {
        return NodeBuilder.create(id, name, CW);
    }

    private static NodeBuilder player(String id, String name) {
        return NodeBuilder.create(id, name, CP);
    }

    private static NodeBuilder data(String id, String name) {
        return NodeBuilder.create(id, name, CD);
    }

    private static NodeBuilder time(String id, String name) {
        return NodeBuilder.create(id, name, CT);
    }

    private static NodeBuilder misc(String id, String name) {
        return NodeBuilder.create(id, name, CM);
    }

    // ================================================================
    //  Node factories
    // ================================================================

    public static BuiltinNode runCommand() {
        return basic("command.run", "Run Command")
                .execInput("In").input(STRING, "Command", "say Hello!").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, c.resolvePinAs(n.inputPin("Command"), String.class, "")));
    }

    public static BuiltinNode say() {
        return basic("command.say", "Say")
                .execInput("In").input(STRING, "Message", "Hello!").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "say " + c.resolvePinAs(n.inputPin("Message"), String.class, "")));
    }

    public static BuiltinNode tell() {
        return basic("command.tell", "Tell")
                .execInput("In").input(SELECTOR, "Target", "@p").input(STRING, "Message", "Hello!").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "tell " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Message"), String.class, "")));
    }

    public static BuiltinNode me() {
        return basic("command.me", "Me")
                .execInput("In").input(STRING, "Message", " did something").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "me " + c.resolvePinAs(n.inputPin("Message"), String.class, "")));
    }

    public static BuiltinNode reload() {
        return basic("command.reload", "Reload")
                .execInput("In").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "reload"));
    }

    // --- Entity ---

    public static BuiltinNode summon() {
        return entity("command.summon", "Summon")
                .execInput("In").input(STRING, "Entity Type", "minecraft:pig")
                .input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(NBT, "NBT", "").execOutput("Continue")
                .build((n, e, g, c) -> {
                    String type = c.resolvePinAs(n.inputPin("Entity Type"), String.class, "minecraft:pig");
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    String nbt = c.resolvePinAs(n.inputPin("NBT"), String.class, "");
                    runCmd(n, e, g, c, "summon " + type + " " + formatVec(pos) + (nbt.isEmpty() ? "" : " " + nbt));
                });
    }

    public static BuiltinNode kill() {
        return entity("command.kill", "Kill")
                .execInput("In").input(SELECTOR, "Target", "@s").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "kill " + resolveSelector(n, c, "Target")));
    }

    public static BuiltinNode damage() {
        return entity("command.damage", "Damage")
                .execInput("In").input(SELECTOR, "Target", "@s")
                .input(FLOAT, "Amount", 1f).input(STRING, "Damage Type", "minecraft:generic").execOutput("Continue")
                .build((n, e, g, c) -> {
                    float amt = c.resolvePinAs(n.inputPin("Amount"), Float.class, 1f);
                    String dt = c.resolvePinAs(n.inputPin("Damage Type"), String.class, "minecraft:generic");
                    runCmd(n, e, g, c, "damage " + resolveSelector(n, c, "Target") + " " + amt + " " + dt);
                });
    }

    public static BuiltinNode teleport() {
        return entity("command.tp", "Teleport")
                .execInput("In").input(SELECTOR, "Target", "@s")
                .input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(FLOAT, "Yaw", 0f).input(FLOAT, "Pitch", 0f).execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    float yaw = c.resolvePinAs(n.inputPin("Yaw"), Float.class, 0f), pitch = c.resolvePinAs(n.inputPin("Pitch"), Float.class, 0f);
                    runCmd(n, e, g, c, "tp " + resolveSelector(n, c, "Target") + " " + formatVec(pos) + " " + yaw + " " + pitch);
                });
    }

    public static BuiltinNode teleportTo() {
        return entity("command.tp_to", "Teleport To Entity")
                .execInput("In").input(SELECTOR, "Target", "@s").input(SELECTOR, "Destination", "@p").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "tp " + resolveSelector(n, c, "Target") + " " + resolveSelector(n, c, "Destination")));
    }

    public static BuiltinNode tagAdd() {
        return entity("command.tag_add", "Add Tag")
                .execInput("In").input(SELECTOR, "Target", "@s").input(STRING, "Tag", "myTag").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "tag " + resolveSelector(n, c, "Target") + " add " + c.resolvePinAs(n.inputPin("Tag"), String.class, "")));
    }

    public static BuiltinNode tagRemove() {
        return entity("command.tag_remove", "Remove Tag")
                .execInput("In").input(SELECTOR, "Target", "@s").input(STRING, "Tag", "myTag").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "tag " + resolveSelector(n, c, "Target") + " remove " + c.resolvePinAs(n.inputPin("Tag"), String.class, "")));
    }

    public static BuiltinNode entityData() {
        return entity("command.entity_data", "Entity Data")
                .execInput("In").input(SELECTOR, "Target", "@s").input(NBT, "NBT", "{}").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "data merge entity " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("NBT"), String.class, "{}")));
    }

    // --- World ---

    public static BuiltinNode setBlock() {
        return world("command.setblock", "Set Block")
                .execInput("In").input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(BLOCK_STATE, "Block").input(FILL_MODE, "Mode", "replace").execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    String block = c.resolvePinAs(n.inputPin("Block"), String.class, "minecraft:stone");
                    runCmd(n, e, g, c, "setblock " + formatBlockPos(pos) + " " + block + " " + c.resolvePinAs(n.inputPin("Mode"), String.class, "replace"));
                });
    }

    public static BuiltinNode fill() {
        return world("command.fill", "Fill")
                .execInput("In").input(VEC3, "From", new Vec3(0, 0, 0)).input(VEC3, "To", new Vec3(0, 0, 0))
                .input(BLOCK_STATE, "Block").input(FILL_MODE, "Mode", "replace").execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 from = resolveVec3(n, c, "From", new Vec3(0, 0, 0));
                    Vec3 to = resolveVec3(n, c, "To", new Vec3(0, 0, 0));
                    String block = c.resolvePinAs(n.inputPin("Block"), String.class, "minecraft:stone");
                    runCmd(n, e, g, c, "fill " + formatBlockPos(from) + " " + formatBlockPos(to) + " " + block + " " + c.resolvePinAs(n.inputPin("Mode"), String.class, "replace"));
                });
    }

    public static BuiltinNode clone_() {
        return world("command.clone", "Clone")
                .execInput("In").input(VEC3, "From", new Vec3(0, 0, 0))
                .input(VEC3, "To", new Vec3(0, 0, 0)).input(VEC3, "Dest", new Vec3(0, 0, 0))
                .input(CLONE_MODE, "Mode", "normal").execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 from = resolveVec3(n, c, "From", new Vec3(0, 0, 0));
                    Vec3 to = resolveVec3(n, c, "To", new Vec3(0, 0, 0));
                    Vec3 dest = resolveVec3(n, c, "Dest", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "clone " + formatBlockPos(from) + " " + formatBlockPos(to) + " " + formatBlockPos(dest) + " " + c.resolvePinAs(n.inputPin("Mode"), String.class, "normal"));
                });
    }

    public static BuiltinNode destroy() {
        return world("command.destroy", "Destroy Block")
                .execInput("In").input(VEC3, "Position", new Vec3(0, 0, 0)).execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "setblock " + formatBlockPos(pos) + " minecraft:air destroy");
                });
    }

    // --- Player ---

    public static BuiltinNode give() {
        return player("command.give", "Give")
                .execInput("In").input(SELECTOR, "Target", "@p").input(ITEM_STACK, "Item").input(INT, "Count", 1).execOutput("Continue")
                .build((n, e, g, c) -> {
                    String item = c.resolvePinAs(n.inputPin("Item"), String.class, "minecraft:stone");
                    runCmd(n, e, g, c, "give " + resolveSelector(n, c, "Target") + " " + item + " " + c.resolvePinAs(n.inputPin("Count"), Integer.class, 1));
                });
    }

    public static BuiltinNode clear() {
        return player("command.clear", "Clear Inventory")
                .execInput("In").input(SELECTOR, "Target", "@p").input(ITEM_STACK, "Item").input(INT, "Max Count", -1).execOutput("Continue")
                .build((n, e, g, c) -> {
                    String item = c.resolvePinAs(n.inputPin("Item"), String.class, "");
                    int max = c.resolvePinAs(n.inputPin("Max Count"), Integer.class, -1);
                    runCmd(n, e, g, c, "clear " + resolveSelector(n, c, "Target") + (item.isEmpty() ? "" : " " + item) + (max >= 0 ? " " + max : ""));
                });
    }

    public static BuiltinNode effectGive() {
        return player("command.effect_give", "Give Effect")
                .execInput("In").input(SELECTOR, "Target", "@s")
                .input(STRING, "Effect", "minecraft:speed").input(INT, "Duration", 30)
                .input(INT, "Amplifier", 0).input(BOOL, "Hide Particles", false).execOutput("Continue")
                .build((n, e, g, c) -> {
                    String eff = c.resolvePinAs(n.inputPin("Effect"), String.class, "minecraft:speed");
                    int dur = c.resolvePinAs(n.inputPin("Duration"), Integer.class, 30);
                    int amp = c.resolvePinAs(n.inputPin("Amplifier"), Integer.class, 0);
                    boolean hide = c.resolvePinAs(n.inputPin("Hide Particles"), Boolean.class, false);
                    runCmd(n, e, g, c, "effect give " + resolveSelector(n, c, "Target") + " " + eff + " " + dur + " " + amp + (hide ? " true" : ""));
                });
    }

    public static BuiltinNode effectClear() {
        return player("command.effect_clear", "Clear Effect")
                .execInput("In").input(SELECTOR, "Target", "@s").input(STRING, "Effect", "").execOutput("Continue")
                .build((n, e, g, c) -> {
                    String eff = c.resolvePinAs(n.inputPin("Effect"), String.class, "");
                    runCmd(n, e, g, c, "effect clear " + resolveSelector(n, c, "Target") + (eff.isEmpty() ? "" : " " + eff));
                });
    }

    public static BuiltinNode experienceAdd() {
        return player("command.xp_add", "Add Experience")
                .execInput("In").input(SELECTOR, "Target", "@p").input(INT, "Amount", 1)
                .input(BOOL, "Is Points", false).execOutput("Continue")
                .build((n, e, g, c) -> {
                    boolean pts = c.resolvePinAs(n.inputPin("Is Points"), Boolean.class, false);
                    runCmd(n, e, g, c, "experience add " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Amount"), Integer.class, 1) + (pts ? " points" : " levels"));
                });
    }

    public static BuiltinNode experienceSet() {
        return player("command.xp_set", "Set Experience")
                .execInput("In").input(SELECTOR, "Target", "@p").input(INT, "Amount", 0)
                .input(BOOL, "Is Points", false).execOutput("Continue")
                .build((n, e, g, c) -> {
                    boolean pts = c.resolvePinAs(n.inputPin("Is Points"), Boolean.class, false);
                    runCmd(n, e, g, c, "experience set " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Amount"), Integer.class, 0) + (pts ? " points" : " levels"));
                });
    }

    public static BuiltinNode gameMode() {
        return player("command.gamemode", "Game Mode")
                .execInput("In").input(GAMEMODE, "Mode", "survival").input(SELECTOR, "Target", "@s").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "gamemode " + c.resolvePinAs(n.inputPin("Mode"), String.class, "survival") + " " + resolveSelector(n, c, "Target")));
    }

    public static BuiltinNode titleSend() {
        return player("command.title", "Send Title")
                .execInput("In").input(SELECTOR, "Target", "@p").input(TITLE_TYPE, "Type", "title")
                .input(STRING, "Text", "Hello!").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "title " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Type"), String.class, "title") + " " + c.resolvePinAs(n.inputPin("Text"), String.class, "Hello!")));
    }

    public static BuiltinNode spawnPoint() {
        return player("command.spawnpoint", "Set Spawn Point")
                .execInput("In").input(SELECTOR, "Target", "@p")
                .input(VEC3, "Position", new Vec3(0, 0, 0)).execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "spawnpoint " + resolveSelector(n, c, "Target") + " " + formatBlockPos(pos));
                });
    }

    public static BuiltinNode setWorldSpawn() {
        return player("command.setworldspawn", "Set World Spawn")
                .execInput("In").input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(FLOAT, "Angle", 0f).execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "setworldspawn " + formatBlockPos(pos) + " " + c.resolvePinAs(n.inputPin("Angle"), Float.class, 0f));
                });
    }

    // --- Data ---

    public static BuiltinNode dataMergeBlock() {
        return data("command.data_merge_block", "Data Merge Block")
                .execInput("In").input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(NBT, "NBT", "{}").execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "data merge block " + formatBlockPos(pos) + " " + c.resolvePinAs(n.inputPin("NBT"), String.class, "{}"));
                });
    }

    public static BuiltinNode dataMergeStorage() {
        return data("command.data_merge_storage", "Data Merge Storage")
                .execInput("In").input(STRING, "Storage", "minecraft:my_storage").input(NBT, "NBT", "{}").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "data merge storage " + c.resolvePinAs(n.inputPin("Storage"), String.class, "") + " " + c.resolvePinAs(n.inputPin("NBT"), String.class, "{}")));
    }

    public static BuiltinNode dataRemove() {
        return data("command.data_remove", "Data Remove")
                .execInput("In").input(STRING, "Target Type", "entity").input(SELECTOR, "Target", "@s")
                .input(STRING, "Path", "").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "data remove " + c.resolvePinAs(n.inputPin("Target Type"), String.class, "entity") + " " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Path"), String.class, "")));
    }

    // --- Time & Weather ---

    public static BuiltinNode timeSet() {
        return time("command.time_set", "Set Time")
                .execInput("In").input(TIME_TYPE, "Type", "daytime").input(INT, "Value", 1000).execOutput("Continue")
                .build((n, e, g, c) -> {
                    String type = c.resolvePinAs(n.inputPin("Type"), String.class, "daytime");
                    int val = c.resolvePinAs(n.inputPin("Value"), Integer.class, 1000);
                    String cmd = switch (type) {
                        case "day" -> "time set day";
                        case "night" -> "time set night";
                        default -> "time set " + val;
                    };
                    runCmd(n, e, g, c, cmd);
                });
    }

    public static BuiltinNode timeAdd() {
        return time("command.time_add", "Add Time")
                .execInput("In").input(INT, "Ticks", 100).execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "time add " + c.resolvePinAs(n.inputPin("Ticks"), Integer.class, 100)));
    }

    public static BuiltinNode weather() {
        return time("command.weather", "Set Weather")
                .execInput("In").input(WEATHER, "Type", "clear").input(INT, "Duration", -1).execOutput("Continue")
                .build((n, e, g, c) -> {
                    int dur = c.resolvePinAs(n.inputPin("Duration"), Integer.class, -1);
                    runCmd(n, e, g, c, "weather " + c.resolvePinAs(n.inputPin("Type"), String.class, "clear") + (dur >= 0 ? " " + dur : ""));
                });
    }

    // --- Misc ---

    public static BuiltinNode difficulty() {
        return misc("command.difficulty", "Set Difficulty")
                .execInput("In").input(DIFFICULTY, "Difficulty", "easy").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "difficulty " + c.resolvePinAs(n.inputPin("Difficulty"), String.class, "easy")));
    }

    public static BuiltinNode enchant() {
        return misc("command.enchant", "Enchant")
                .execInput("In").input(SELECTOR, "Target", "@s").input(STRING, "Enchantment", "minecraft:sharpness")
                .input(INT, "Level", 1).execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "enchant " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Enchantment"), String.class, "minecraft:sharpness") + " " + c.resolvePinAs(n.inputPin("Level"), Integer.class, 1)));
    }

    public static BuiltinNode particle() {
        return misc("command.particle", "Spawn Particle")
                .execInput("In").input(STRING, "Particle", "minecraft:poof")
                .input(VEC3, "Position", new Vec3(0, 0, 0)).input(VEC3, "Delta", new Vec3(0, 0, 0))
                .input(FLOAT, "Speed", 0f).input(INT, "Count", 1).execOutput("Continue")
                .build((n, e, g, c) -> {
                    String p = c.resolvePinAs(n.inputPin("Particle"), String.class, "minecraft:poof");
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    Vec3 delta = resolveVec3(n, c, "Delta", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "particle " + p + " " + formatVec(pos) + " " + formatVec(delta) + " " + c.resolvePinAs(n.inputPin("Speed"), Float.class, 0f) + " " + c.resolvePinAs(n.inputPin("Count"), Integer.class, 1));
                });
    }

    public static BuiltinNode playSound() {
        return misc("command.playsound", "Play Sound")
                .execInput("In").input(STRING, "Sound", "minecraft:entity.pig.ambient").input(SOUND_SOURCE, "Source", "master")
                .input(SELECTOR, "Target", "@p").input(VEC3, "Position", new Vec3(0, 0, 0))
                .input(FLOAT, "Volume", 1f).input(FLOAT, "Pitch", 1f).execOutput("Continue")
                .build((n, e, g, c) -> {
                    Vec3 pos = resolveVec3(n, c, "Position", new Vec3(0, 0, 0));
                    runCmd(n, e, g, c, "playsound " + c.resolvePinAs(n.inputPin("Sound"), String.class, "") + " " + c.resolvePinAs(n.inputPin("Source"), String.class, "master") + " " + resolveSelector(n, c, "Target") + " " + formatVec(pos) + " " + c.resolvePinAs(n.inputPin("Volume"), Float.class, 1f) + " " + c.resolvePinAs(n.inputPin("Pitch"), Float.class, 1f));
                });
    }

    public static BuiltinNode stopSound() {
        return misc("command.stopsound", "Stop Sound")
                .execInput("In").input(SELECTOR, "Target", "@p").input(SOUND_SOURCE, "Source", "master")
                .input(STRING, "Sound", "").execOutput("Continue")
                .build((n, e, g, c) -> {
                    String sound = c.resolvePinAs(n.inputPin("Sound"), String.class, "");
                    runCmd(n, e, g, c, "stopsound " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Source"), String.class, "master") + (sound.isEmpty() ? "" : " " + sound));
                });
    }

    public static BuiltinNode recipeGive() {
        return misc("command.recipe_give", "Give Recipe")
                .execInput("In").input(SELECTOR, "Target", "@p").input(STRING, "Recipe", "*").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "recipe give " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Recipe"), String.class, "*")));
    }

    public static BuiltinNode recipeTake() {
        return misc("command.recipe_take", "Take Recipe")
                .execInput("In").input(SELECTOR, "Target", "@p").input(STRING, "Recipe", "*").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "recipe take " + resolveSelector(n, c, "Target") + " " + c.resolvePinAs(n.inputPin("Recipe"), String.class, "*")));
    }

    public static BuiltinNode schedule() {
        return misc("command.schedule", "Schedule Function")
                .execInput("In").input(STRING, "Function", "minecraft:some_function").input(INT, "Time", 1)
                .input(TIME_UNIT, "Time Unit", "s").input(BOOL, "Append", false).execOutput("Continue")
                .build((n, e, g, c) -> {
                    boolean append = c.resolvePinAs(n.inputPin("Append"), Boolean.class, false);
                    runCmd(n, e, g, c, "schedule function " + c.resolvePinAs(n.inputPin("Function"), String.class, "") + " " + c.resolvePinAs(n.inputPin("Time"), Integer.class, 1) + c.resolvePinAs(n.inputPin("Time Unit"), String.class, "s") + (append ? " append" : ""));
                });
    }

    public static BuiltinNode spreadPlayers() {
        return misc("command.spreadplayers", "Spread Players")
                .execInput("In").input(FLOAT, "X", 0f).input(FLOAT, "Z", 0f)
                .input(FLOAT, "Spread Distance", 5f).input(FLOAT, "Max Range", 20f)
                .input(BOOL, "Respect Teams", true).input(SELECTOR, "Target", "@p").execOutput("Continue")
                .build((n, e, g, c) -> {
                    boolean teams = c.resolvePinAs(n.inputPin("Respect Teams"), Boolean.class, true);
                    runCmd(n, e, g, c, "spreadplayers " + c.resolvePinAs(n.inputPin("X"), Float.class, 0f) + " " + c.resolvePinAs(n.inputPin("Z"), Float.class, 0f) + " " + c.resolvePinAs(n.inputPin("Spread Distance"), Float.class, 5f) + " " + c.resolvePinAs(n.inputPin("Max Range"), Float.class, 20f) + " " + (teams) + " " + resolveSelector(n, c, "Target"));
                });
    }

    public static BuiltinNode lootGive() {
        return misc("command.loot_give", "Loot Give")
                .execInput("In").input(SELECTOR, "Target", "@p").input(STRING, "Loot Table", "minecraft:chests/simple_dungeon").execOutput("Continue")
                .build((n, e, g, c) -> runCmd(n, e, g, c, "loot give " + resolveSelector(n, c, "Target") + " loot " + c.resolvePinAs(n.inputPin("Loot Table"), String.class, "")));
    }

    public static BuiltinNode worldBorder() {
        return misc("command.worldborder", "World Border")
                .execInput("In").input(STRING, "Action", "set").input(FLOAT, "Value", 100f)
                .input(INT, "Time", 0).execOutput("Continue")
                .build((n, e, g, c) -> {
                    int time = c.resolvePinAs(n.inputPin("Time"), Integer.class, 0);
                    float val = c.resolvePinAs(n.inputPin("Value"), Float.class, 100f);
                    runCmd(n, e, g, c, "worldborder " + c.resolvePinAs(n.inputPin("Action"), String.class, "set") + " " + Math.round(val) + (time > 0 ? " " + time : ""));
                });
    }
}
