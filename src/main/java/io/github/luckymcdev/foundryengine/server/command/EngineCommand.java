package io.github.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

public interface EngineCommand {
    /**
     * Define the command structure here.
     */
    LiteralArgumentBuilder<CommandSourceStack> build();

    /**
     * Shared helper for permission checks.
     */
    default boolean isAdmin(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }
}