package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.cutscene.storage.CutsceneSavedData;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import de.luckymcdev.foundryengine.common.cutscene.util.ServerCutsceneManager;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CutsceneCommand implements EngineCommand {
    private static final SuggestionProvider<CommandSourceStack> CUTSCENE_SUGGESTIONS = (context, builder) -> {
        ServerLevel level = context.getSource().getLevel();
        return SharedSuggestionProvider.suggestResource(
                CutsceneSavedData.get(level).getSuggestions(),
                builder
        );
    };

    private static final SuggestionProvider<CommandSourceStack> EASING_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    java.util.Arrays.stream(LerpType.values()).map(Enum::name),
                    builder
            );

    private static int getOptionalInt(CommandContext<CommandSourceStack> ctx, String key, int fallback) {
        try {
            return ctx.getArgument(key, Integer.class);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String getOptionalString(CommandContext<CommandSourceStack> ctx, String key, String fallback) {
        try {
            return StringArgumentType.getString(ctx, key);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cutscene")
                .requires(this::isGamemaster);

        // resetAll confirm
        root.then(Commands.literal("resetAll")
                .then(Commands.literal("confirm")
                        .executes(this::resetAll)));

        // list
        root.then(Commands.literal("list")
                .executes(this::listCutscenes));

        // add <name>
        root.then(Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(this::addCutscene)));

        // remove <name>
        root.then(Commands.literal("remove")
                .then(Commands.argument("name", IdentifierArgument.id())
                        .suggests(CUTSCENE_SUGGESTIONS)
                        .executes(this::removeCutscene)));

        // linearize <name>
        root.then(Commands.literal("linearize")
                .then(Commands.argument("name", IdentifierArgument.id())
                        .suggests(CUTSCENE_SUGGESTIONS)
                        .executes(this::linearizeCutscene)));

        // cancel <player>
        root.then(Commands.literal("cancel")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(this::cancelCutscene)));

        // play <player> <name> <length> [easing] [holdStart] [holdEnd] or play <player> <name>
        var play = Commands.literal("play")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("name", IdentifierArgument.id())
                                .suggests(CUTSCENE_SUGGESTIONS)
                                .executes(ctx -> play(ctx))
                                .then(Commands.argument("length", TimeArgument.time(0))
                                        .executes(ctx -> play(ctx))
                                        .then(Commands.argument("easing", StringArgumentType.word())
                                                .suggests(EASING_SUGGESTIONS)
                                                .executes(ctx -> play(ctx))
                                                .then(Commands.argument("holdStart", TimeArgument.time(0))
                                                        .executes(ctx -> play(ctx))
                                                        .then(Commands.argument("holdEnd", TimeArgument.time(0))
                                                                .executes(ctx -> play(ctx))))))));
        root.then(play);
        return root;
    }

    private int resetAll(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        CutsceneSavedData data = CutsceneSavedData.get(level);
        data.setData(new CompoundTag());
        data.syncToClients(level);
        sendSuccess(ctx, "All cutscenes removed.", true);
        return 1;
    }

    private int listCutscenes(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        List<Cutscene> cutscenes = CutsceneSavedData.get(level).getCutscenes();
        sendInfo(ctx, "There are " + cutscenes.size() + " cutscenes:");
        for (Cutscene cutscene : cutscenes) {
            sendInfo(ctx, "  " + cutscene.getName());
        }
        return 1;
    }

    private int addCutscene(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = getPlayer(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        ServerLevel level = ctx.getSource().getLevel();
        CutsceneSavedData data = CutsceneSavedData.get(level);
        List<Cutscene> cutscenes = new ArrayList<>(data.getCutscenes());

        for (Cutscene cutscene : cutscenes) {
            if (cutscene.getName().equals(name)) {
                sendFailure(ctx, "Cutscene of name [" + name + "] already exists!");
                return 0;
            }
        }

        BezierPath path = new BezierPath(player.getEyePosition());
        Vec2 rot = new Vec2(player.getXRot(), player.getYRot());
        cutscenes.add(new Cutscene(name, rot, rot, path));

        data.setCutscenes(cutscenes);
        data.syncToClients(level);
        sendSuccess(ctx, "Added cutscene: " + name, true);
        return 1;
    }

    private int removeCutscene(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        Identifier id = IdentifierArgument.getId(ctx, "name");
        String name = id.getPath();
        CutsceneSavedData data = CutsceneSavedData.get(level);
        List<Cutscene> cutscenes = new ArrayList<>(data.getCutscenes());
        boolean removed = cutscenes.removeIf(c -> c.getName().equals(name));
        if (!removed) {
            sendFailure(ctx, "No cutscene found with name: " + name);
            return 0;
        }
        data.setCutscenes(cutscenes);
        data.syncToClients(level);
        sendSuccess(ctx, "Removed cutscene: " + name, true);
        return 1;
    }

    private int linearizeCutscene(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        Identifier id = IdentifierArgument.getId(ctx, "name");
        String name = id.getPath();
        CutsceneSavedData data = CutsceneSavedData.get(level);
        List<Cutscene> cutscenes = new ArrayList<>(data.getCutscenes());

        Cutscene target = null;
        for (Cutscene c : cutscenes) {
            if (c.getName().equals(name)) {
                target = c;
                break;
            }
        }
        if (target == null) {
            sendFailure(ctx, "No cutscene found with name: " + name);
            return 0;
        }

        if (target.path.getPoints().size() != 4) {
            sendFailure(ctx, "Cutscene must have exactly 2 path nodes to linearize.");
            return 0;
        }

        Vec3 p1 = target.path.getPoints().getFirst().getPos();
        Vec3 p2 = target.path.getPoints().getLast().getPos();
        Vec3 tangent = p2.add(p1.subtract(p2).scale(0.5));
        target.path.getPoints().get(1).setPos(tangent);
        target.path.getPoints().get(2).setPos(tangent);

        data.setCutscenes(cutscenes);
        data.syncToClients(level);
        sendInfo(ctx, "Linearized cutscene: " + name);
        return 1;
    }

    private int cancelCutscene(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        if (!ServerCutsceneManager.inCutscene(player)) {
            sendFailure(ctx, player.getName().getString() + " is not viewing any cutscenes.");
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Cancel", true);
        PacketDistributor.sendToPlayer(player, new CutscenePacket(tag));
        ServerCutsceneManager.cancelCutscene(player);
        return 1;
    }

    private int play(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
        Identifier id = IdentifierArgument.getId(ctx, "name");
        String name = id.getPath();

        CutsceneSavedData data = CutsceneSavedData.get(level);
        Cutscene cutscene = data.getCutscenes().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);

        if (cutscene == null) {
            sendFailure(ctx, "No cutscene found with name: " + name);
            return 0;
        }

        int length = getOptionalInt(ctx, "length", cutscene.getDefaultLength());
        int holdStart = getOptionalInt(ctx, "holdStart", cutscene.getDefaultHoldStart());
        int holdEnd = getOptionalInt(ctx, "holdEnd", cutscene.getDefaultHoldEnd());

        String easingName = getOptionalString(ctx, "easing", cutscene.getDefaultEasing());
        LerpType easing = LerpType.fromString(easingName);

        CompoundTag tag = new CompoundTag();
        tag.putString("PlayName", name);
        tag.putString("LerpType", easing.name());
        tag.putInt("Length", length);
        tag.putInt("holdStart", holdStart);
        tag.putInt("holdEnd", holdEnd);

        data.syncToPlayer(targetPlayer);
        int total = length + holdStart + holdEnd;
        ServerCutsceneManager.addInstance(targetPlayer, total);
        PacketDistributor.sendToPlayer(targetPlayer, new CutscenePacket(tag));

        return 1;
    }
}