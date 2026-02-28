package io.github.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.luckymcdev.foundryengine.server.command.builtin.DumpCommand;
import io.github.luckymcdev.foundryengine.server.command.builtin.HandCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

public class FoundryCommands {
    private static final List<EngineCommand> COMMANDS = List.of(
            new DumpCommand(),
            new HandCommand()
    );

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (EngineCommand command : COMMANDS) {
            dispatcher.register(Commands.literal("foundry").then(
                    command.build()
            ));
        }
    }
}