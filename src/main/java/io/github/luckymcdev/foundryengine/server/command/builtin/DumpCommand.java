package io.github.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class DumpCommand implements EngineCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

    private static int dumpRegistries(CommandContext<CommandSourceStack> context, Optional<Identifier> filter) {
        CommandSourceStack source = context.getSource();
        LocalDateTime now = LocalDateTime.now();

        String fileName = filter.map(id -> "dump-" + id.getPath().replace("/", "_") + "-" + now.format(FILE_TIME_FORMAT) + ".txt")
                .orElse("dump-all-" + now.format(FILE_TIME_FORMAT) + ".txt");

        Path outputPath = Common.DUMPS.resolve(fileName);

        try {
            Files.createDirectories(outputPath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                // Header Block
                writer.write("================================================================================\n");
                writer.write(" FOUNDRY ENGINE - REGISTRY DUMP\n");
                writer.write(" Generated: " + now.format(TIME_FORMAT) + "\n");
                writer.write(" Game Version: " + SharedConstants.getCurrentVersion().id() + "\n");
                writer.write(" Mode: " + (filter.map(identifier -> "Filtered (" + identifier + ")").orElse("Full Dump")) + "\n");
                writer.write("================================================================================\n\n");

                if (filter.isEmpty()) {
                    writer.write("TABLE OF CONTENTS\n");
                    writer.write("-----------------\n");
                    BuiltInRegistries.REGISTRY.keySet().stream().sorted().forEach(id -> {
                        try {
                            writer.write("- " + id + "\n");
                        } catch (IOException ignored) {
                        }
                    });
                    writer.write("\n\n");
                }

                // Registry Content
                BuiltInRegistries.REGISTRY.entrySet().forEach(entry -> {
                    Identifier registryId = entry.getKey().identifier();
                    if (filter.isPresent() && !filter.get().equals(registryId)) return;

                    Registry<?> registry = entry.getValue();
                    try {
                        writer.write(">>> REGISTRY: " + registryId + "\n");
                        writer.write("    Total Entries: " + registry.size() + "\n");
                        writer.write("--------------------------------------------------------------------------------\n");

                        List<Identifier> sortedKeys = registry.keySet().stream().sorted().toList();
                        AtomicInteger index = new AtomicInteger(1);

                        for (Identifier key : sortedKeys) {
                            writer.write(String.format("[%03d] %s\n", index.getAndIncrement(), key.toString()));
                        }

                        writer.write("--------------------------------------------------------------------------------\n\n");
                    } catch (IOException e) {
                        LOGGER.error("Failed to write registry {} to file", registryId, e);
                    }
                });

                writer.write("End of Dump.\n");
            }

            source.sendSuccess(() -> Component.literal("Registry dump generated: " + outputPath.getFileName()), true);
            return 1;
        } catch (IOException e) {
            LOGGER.error("Could not create dump file", e);
            source.sendFailure(Component.literal("Failed to generate dump file. Check console."));
            return 0;
        }
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("dump")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.literal("all")
                        .executes(context -> dumpRegistries(context, Optional.empty()))
                )
                .then(Commands.literal("registry")
                        .then(Commands.argument("registry_name", IdentifierArgument.id())
                                .suggests((ctx, builder) -> {
                                    BuiltInRegistries.REGISTRY.keySet().forEach(id -> builder.suggest(id.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    Identifier target = IdentifierArgument.getId(context, "registry_name");
                                    return dumpRegistries(context, Optional.of(target));
                                })
                        )
                );
    }
}