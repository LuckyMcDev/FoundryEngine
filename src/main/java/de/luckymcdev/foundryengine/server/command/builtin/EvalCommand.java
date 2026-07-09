package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.script.ScriptConfig;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EvalCommand implements EngineCommand {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
		return Commands.literal("eval")
			.requires(src -> is(src, StartupConfig.EVAL_COMMAND_PERMISSION.get()))
			.then(Commands.argument("code", StringArgumentType.greedyString()).executes(this::execute));
	}

	private int execute(CommandContext<CommandSourceStack> ctx) {
		if (!StartupConfig.EVAL_COMMAND_ENABLED.get()) {
			sendFailure(ctx, "Eval command is disabled in the config.");
			return 0;
		}
		String code = StringArgumentType.getString(ctx, "code");

		ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
		PrintStream capturePs = new PrintStream(capturedOut);
		PrintStream oldOut = System.out;
		System.setOut(capturePs);

		try {
			Binding binding = new Binding();

			binding.setVariable("player", ctx.getSource().getPlayer());
			binding.setVariable("level", ctx.getSource().getLevel());
			binding.setVariable("server", ctx.getSource().getServer());
			binding.setVariable("source", ctx.getSource());

			GroovyShell shell = new GroovyShell(
					Thread.currentThread().getContextClassLoader(),
					binding,
					ScriptConfig.createCompilerConfig()
			);

			Object result = shell.evaluate(code);
			System.setOut(oldOut);

			StringBuilder response = new StringBuilder();
			String printed = capturedOut.toString().trim();
			if (!printed.isEmpty()) {
				response.append(printed);
			}
			if (result != null) {
				if (!response.isEmpty()) {
					response.append("\n");
				}
				response.append("Result: ").append(result);
			} else if (response.isEmpty()) {
				response.append("Executed successfully (null)");
			}

			sendSuccess(ctx, response.toString(), false);
		} catch (Exception e) {
			System.setOut(oldOut);
			LOGGER.error("Error evaluating code: {}", e.getMessage());
			sendFailure(ctx, "Error: " + e.getMessage());
		}
		return 1;
	}
}