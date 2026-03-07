package io.github.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public interface EngineCommand {
    /**
     * Define the command structure here.
     */
    LiteralArgumentBuilder<CommandSourceStack> build();

    default boolean isModerator(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
    }

    default boolean isGamemaster(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    default boolean isAdmin(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }

    default boolean isOwner(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_OWNER);
    }

    /**
     * Quickly gets the player from the context or throws the standard 'Player Only' exception.
     */
    default ServerPlayer getPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    /**
     * Sends a successful message with automatic color formatting.
     */
    default void sendSuccess(CommandContext<CommandSourceStack> ctx, String message, boolean broadcastToOps) {
        ctx.getSource().sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.GREEN), broadcastToOps);
    }

    /**
     * Sends a failure/error message.
     */
    default void sendFailure(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendFailure(Component.literal(message).withStyle(ChatFormatting.DARK_RED));
    }

    /**
     * Sends a system/info message (standard gray).
     */
    default void sendInfo(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.GRAY), false);
    }
}