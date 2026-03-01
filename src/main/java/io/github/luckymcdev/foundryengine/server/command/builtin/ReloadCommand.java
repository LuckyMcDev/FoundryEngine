package io.github.luckymcdev.foundryengine.server.command.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.luckymcdev.foundryengine.common.Common;
import io.github.luckymcdev.foundryengine.server.command.EngineCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public class ReloadCommand implements EngineCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("reload")
                .requires(stack -> stack.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
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