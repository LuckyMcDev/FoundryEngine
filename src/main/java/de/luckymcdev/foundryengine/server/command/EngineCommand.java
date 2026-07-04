package de.luckymcdev.foundryengine.server.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;

/**
 * Util for creating commands for the /engine command.
 */
public interface EngineCommand {
    /**
     * Define the command structure here.
     */
    LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext context);

    /**
     * Checks if the source is Moderator.
     */
    default boolean isModerator(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR);
    }

    /**
     * Checks if the source is Game master.
     */
    default boolean isGamemaster(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /**
     * Checks if the source is Admin.
     */
    default boolean isAdmin(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
    }

    /**
     * Checks if the source is Owner.
     */
    default boolean isOwner(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_OWNER);
    }

    /**
     * Checks if the source has a permission based on an int level.
     */
    default boolean is(CommandSourceStack source, int permissionLevel) {
        PermissionLevel level = PermissionLevel.byId(permissionLevel);
        return source.permissions().hasPermission(new Permission.HasCommandLevel(level));
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
        ctx.getSource().sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.DARK_GREEN), broadcastToOps);
    }

    default void sendSuccess(CommandContext<CommandSourceStack> ctx, Component message, boolean broadcastToOps) {
        ctx.getSource().sendSuccess(() -> message.copy().withStyle(ChatFormatting.DARK_GREEN), broadcastToOps);
    }

    /**
     * Sends a failure/error message.
     */
    default void sendFailure(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendFailure(Component.literal(message).withStyle(ChatFormatting.DARK_RED));
    }

    default void sendFailure(CommandContext<CommandSourceStack> ctx, Component message) {
        ctx.getSource().sendFailure(message.copy().withStyle(ChatFormatting.DARK_RED));
    }

    /**
     * Sends a system/info message (standard gray).
     */
    default void sendInfo(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal(message).withStyle(ChatFormatting.GRAY), false);
    }

    default void sendInfo(CommandContext<CommandSourceStack> ctx, Component message) {
        ctx.getSource().sendSuccess(() -> message.copy().withStyle(ChatFormatting.GRAY), false);
    }
}