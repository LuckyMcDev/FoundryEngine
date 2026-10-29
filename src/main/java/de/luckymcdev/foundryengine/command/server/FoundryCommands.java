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
	private static final List<EngineCommand> COMMANDS = List.of(
		new DumpCommand(),
		new HandCommand(),
		new ReloadCommand(),
		new StageCommand(),
		new TestCommand(),
		new EvalCommand(),
		new CutsceneCommand(),
		new DialogueCommand(),
		new ScreenEffectCommand(),
		new WaypointCommand(),
		new BlockToDisplayCommand()
	);

	public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		LiteralArgumentBuilder<CommandSourceStack> engineRoot = Commands.literal("engine");
		for (EngineCommand command : COMMANDS) {
			engineRoot.then(command.build(context));
		}
		dispatcher.register(engineRoot);
	}
}