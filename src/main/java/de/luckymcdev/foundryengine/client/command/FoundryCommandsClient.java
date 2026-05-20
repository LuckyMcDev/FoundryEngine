package de.luckymcdev.foundryengine.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.client.command.builtin.GenerateIconsCommand;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

/**
 * Registers commands that reference client-only classes (e.g. Minecraft, Screen).
 * This class is only loaded on the physical client.
 */
public class FoundryCommandsClient {
    private static final List<EngineCommand> COMMANDS = List.of(
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