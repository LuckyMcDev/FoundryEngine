package io.github.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import groovy.lang.GroovyShell;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.config.Config;
import io.github.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EvalCommand implements EngineCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("eval").requires(this::isOwner)
                .then(Commands.argument("code", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String code = StringArgumentType.getString(ctx, "code");

                            if (!Config.Startup.EVAL_COMMAND_ENABLED.get()) {
                                sendFailure(ctx, "Eval command is disabled in the config.");
                                return 0;
                            }

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            PrintStream printStream = new PrintStream(outputStream);
                            PrintStream oldOut = System.out;
                            System.setOut(printStream);

                            CompilerConfiguration config = Common.getBundleManager().getBundleDiscovery().getBundleFactory().getScriptEngineFactory().createCompilerConfiguration();
                            GroovyShell shell = new GroovyShell(config);

                            try {
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
                                    response.append("Success (null)");
                                }

                                sendSuccess(ctx, response.toString(), false);
                            } catch (Exception e) {
                                System.setOut(oldOut);
                                LOGGER.error("Error evaluating code: {}", e.getMessage());
                                sendFailure(ctx, "Error: " + e.getMessage());
                            }
                            return 1;
                        }));
    }
}