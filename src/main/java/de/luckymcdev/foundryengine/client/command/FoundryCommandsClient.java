package de.luckymcdev.foundryengine.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.server.command.builtin.GenerateIconsCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Registers commands that reference client-only classes (e.g. Minecraft, Screen).
 * This class is only loaded on the physical client.
 */
public class FoundryCommandsClient {

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> engineRoot = Commands.literal("engine");
        engineRoot.then(new GenerateIconsCommand().build(event.getBuildContext()));
        event.getDispatcher().register(engineRoot);
    }
}