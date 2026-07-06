package de.luckymcdev.foundryengine.common.util;

import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

/**
 * Pre-defined permission checks for command and chat access levels.
 */
public class PermissionChecks {
	public static final PermissionCheck PASS = PermissionCheck.AlwaysPass.INSTANCE;

	public static final PermissionCheck COMMANDS_MODERATOR = new PermissionCheck.Require(Permissions.COMMANDS_MODERATOR);
	public static final PermissionCheck COMMANDS_GAMEMASTER = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);
	public static final PermissionCheck COMMANDS_ADMIN = new PermissionCheck.Require(Permissions.COMMANDS_ADMIN);
	public static final PermissionCheck COMMANDS_OWNER = new PermissionCheck.Require(Permissions.COMMANDS_OWNER);

	public static final PermissionCheck COMMANDS_ENTITY_SELECTORS = new PermissionCheck.Require(Permissions.COMMANDS_ENTITY_SELECTORS);

	public static final PermissionCheck CHAT_SEND_MESSAGES = new PermissionCheck.Require(Permissions.CHAT_SEND_MESSAGES);
	public static final PermissionCheck CHAT_SEND_COMMANDS = new PermissionCheck.Require(Permissions.CHAT_SEND_COMMANDS);
	public static final PermissionCheck CHAT_RECEIVE_PLAYER_MESSAGES = new PermissionCheck.Require(Permissions.CHAT_RECEIVE_PLAYER_MESSAGES);
	public static final PermissionCheck CHAT_RECEIVE_SYSTEM_MESSAGES = new PermissionCheck.Require(Permissions.CHAT_RECEIVE_SYSTEM_MESSAGES);
}