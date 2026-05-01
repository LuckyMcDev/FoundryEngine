package de.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Command to reload all bundles.
 */
public class ReloadCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        return Commands.literal("reload")
                .requires(this::isAdmin)
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    try {
                        Common.getBundleManager().reload();
                        source.sendSuccess(() -> Component.literal("Scripts reloaded successfully!"), true);
                        return 1;
                    } catch (Exception e) {
                        source.sendFailure(Component.literal("Failed to reload scripts: " + e.getMessage()));
                        return 0;
                    }
                });
    }
}