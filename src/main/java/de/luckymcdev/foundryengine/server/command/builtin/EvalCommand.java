package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.config.StartupConfig;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EvalCommand implements EngineCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Binding GLOBAL_BINDING = new Binding();

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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream oldOut = System.out;
        System.setOut(printStream);

        try {
            Binding binding = GLOBAL_BINDING;

            binding.setVariable("player", ctx.getSource().getPlayer());
            binding.setVariable("level", ctx.getSource().getLevel());
            binding.setVariable("server", ctx.getSource().getServer());
            binding.setVariable("source", ctx.getSource());
            binding.setVariable("tellPlayer", (TellPlayer) message ->
                    ctx.getSource().getPlayer().sendSystemMessage(Component.literal(message))
            );

            GroovyShell shell = new GroovyShell(binding);

            Object result = shell.evaluate(code);
            System.setOut(oldOut);
            String capturedOutput = outputStream.toString().trim();

            StringBuilder response = new StringBuilder();
            if (!capturedOutput.isEmpty()) {
                response.append(capturedOutput);
            }
            if (result != null) {
                if (!response.isEmpty()) response.append("\n");
                response.append("Result: ").append(result);
            } else if (response.isEmpty()) {
                //response.append("Success (null)");
            }

            sendSuccess(ctx, response.toString(), false);
        } catch (Exception e) {
            System.setOut(oldOut);
            LOGGER.error("Error evaluating code: {}", e.getMessage());
            sendFailure(ctx, "Error: " + e.getMessage());
        }
        return 1;
    }

    @FunctionalInterface
    public interface TellPlayer {
        void call(String message);
    }
}