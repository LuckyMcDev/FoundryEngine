package io.github.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.luckymcdev.foundryengine.server.command.builtin.*;
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
            new TestCommand()
    );

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (EngineCommand command : COMMANDS) {
            dispatcher.register(Commands.literal("engine").then(
                    command.build()
            ));
        }
    }
}