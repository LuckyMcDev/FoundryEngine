package de.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.server.command.builtin.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

/**
 * Command Registry.
 */
public class FoundryCommands {
    private static final List<EngineCommand> COMMANDS = List.of(
            new DumpCommand(),
            new HandCommand(),
            new ReloadCommand(),
            new StageCommand(),
            new TestCommand(),
            new EvalCommand(),
            new GenerateIconsCommand()
    );

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> engineRoot = Commands.literal("engine");
        for (EngineCommand command : COMMANDS) {
            engineRoot.then(command.build(context));
        }
        dispatcher.register(engineRoot);
    }
}