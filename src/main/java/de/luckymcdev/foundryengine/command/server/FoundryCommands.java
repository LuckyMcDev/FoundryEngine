package de.luckymcdev.foundryengine.command.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.command.server.builtin.BlockToDisplayCommand;
import de.luckymcdev.foundryengine.command.server.builtin.CutsceneCommand;
import de.luckymcdev.foundryengine.command.server.builtin.DialogueCommand;
import de.luckymcdev.foundryengine.command.server.builtin.DumpCommand;
import de.luckymcdev.foundryengine.command.server.builtin.EvalCommand;
import de.luckymcdev.foundryengine.command.server.builtin.HandCommand;
import de.luckymcdev.foundryengine.command.server.builtin.ReloadCommand;
import de.luckymcdev.foundryengine.command.server.builtin.ScreenEffectCommand;
import de.luckymcdev.foundryengine.command.server.builtin.StageCommand;
import de.luckymcdev.foundryengine.command.server.builtin.TestCommand;
import de.luckymcdev.foundryengine.command.server.builtin.WaypointCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.List;

/**
 * Command Registry.
 *
 * <p>Only server-safe commands live here. Client-only commands
 * are registered separately via {@link de.luckymcdev.foundryengine.command.client.FoundryCommandsClient} on the physical client.</p>
 */
public class FoundryCommands {
	public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		LiteralArgumentBuilder<CommandSourceStack> engineRoot = Commands.literal("engine");
		for (EngineCommand command : commands()) {
			if (command.enabled()) {
				engineRoot.then(command.build(context));
			}
		}
		dispatcher.register(engineRoot);
	}

	private static List<EngineCommand> commands() {
		var commands = new java.util.ArrayList<EngineCommand>();
		commands.add(new DumpCommand());
		commands.add(new HandCommand());
		commands.add(new BlockToDisplayCommand());
		commands.add(new ReloadCommand());
		commands.add(new EvalCommand());
		commands.add(new StageCommand());
		commands.add(new TestCommand());
		commands.add(new CutsceneCommand());
		commands.add(new ScreenEffectCommand());
		commands.add(new DialogueCommand());
		commands.add(new WaypointCommand());
		return commands;
	}
}