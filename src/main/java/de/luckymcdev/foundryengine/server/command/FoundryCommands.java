package de.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.client.command.FoundryCommandsClient;
import de.luckymcdev.foundryengine.server.command.builtin.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

/**
 * Command Registry.
 *
 * <p>Only server-safe commands live here. Client-only commands (e.g. GenerateIconsCommand)
 * are registered separately via {@link FoundryCommandsClient} on the physical client.</p>
 */
public class FoundryCommands {
    private static final List<EngineCommand> COMMANDS = List.of(
            new DumpCommand(),
            new HandCommand(),
            new ReloadCommand(),
            new StageCommand(),
            new TestCommand(),
            new EvalCommand(),
            new CutsceneCommand(),
            new ScreenEffectCommand()
    );

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> engineRoot = Commands.literal("engine");
        for (EngineCommand command : COMMANDS) {
            engineRoot.then(command.build(context));
        }
        dispatcher.register(engineRoot);
    }
}