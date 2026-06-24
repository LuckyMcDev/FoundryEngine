package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;

import de.luckymcdev.foundryengine.common.cutscene.util.ServerScreenEffectManager;
import de.luckymcdev.foundryengine.common.network.packets.sync.ScreenEffectPacket;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collection;

public class ScreenEffectCommand implements EngineCommand {

    private static final String[] KNOWN_EFFECTS = {"none", "black", "circle", "star", "cinematic", "grayscale", "sepia", "depth_vis"};

    private static final SuggestionProvider<CommandSourceStack> EFFECT_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(KNOWN_EFFECTS), builder);

    private static final SuggestionProvider<CommandSourceStack> EASING_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(LerpType.values()).map(Enum::name), builder);

    private static String getOptionalString(CommandContext<CommandSourceStack> ctx, String key, String fallback) {
        try {
            return StringArgumentType.getString(ctx, key);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("screeneffect")
                .requires(this::isGamemaster);

        var players = Commands.argument("players", EntityArgument.players());
        var effect = Commands.argument("effect", StringArgumentType.word()).suggests(EFFECT_SUGGESTIONS);
        var intro = Commands.argument("intro", TimeArgument.time(0));
        var hold = Commands.argument("hold", TimeArgument.time(0)).executes(ctx -> send(ctx, false, false));
        var outro = Commands.argument("outro", TimeArgument.time(0)).executes(ctx -> send(ctx, true, false));
        var easing = Commands.argument("easing", StringArgumentType.word())
                .suggests(EASING_SUGGESTIONS)
                .executes(ctx -> send(ctx, true, false));
        var command = Commands.argument("command", StringArgumentType.greedyString())
                .executes(ctx -> send(ctx, true, true));

        easing.then(command);
        outro.then(easing);
        hold.then(outro);
        intro.then(hold);
        effect.then(intro);
        players.then(effect);
        root.then(players);

        return root;
    }

    private int send(CommandContext<CommandSourceStack> ctx, boolean hasOutro, boolean hasCommand) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
        String effect = StringArgumentType.getString(ctx, "effect");
        int intro = ctx.getArgument("intro", Integer.class);
        int hold = ctx.getArgument("hold", Integer.class);
        int outro = hasOutro ? ctx.getArgument("outro", Integer.class) : intro;

        String easing = getOptionalString(ctx, "easing", LerpType.LINEAR.name());
        String command = hasCommand ? StringArgumentType.getString(ctx, "command") : "";

        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, new ScreenEffectPacket(effect, intro, hold, outro, easing));
            ServerScreenEffectManager.addInstance(player, intro + hold + outro);
            if (!command.isEmpty()) {
                MinecraftServer server = player.level().getServer();
                if (server != null) {
                    var source = player.createCommandSourceStack()
                            .withMaximumPermission(PermissionSet.ALL_PERMISSIONS)
                            .withSuppressedOutput();
                    server.getCommands().performPrefixedCommand(source, command);
                }
            }
        }

        sendInfo(ctx, "Screen effect sent to " + players.size() + " player(s).");
        return players.size();
    }
}
