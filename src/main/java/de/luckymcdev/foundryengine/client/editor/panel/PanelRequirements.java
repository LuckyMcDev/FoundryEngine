package de.luckymcdev.foundryengine.client.editor.panel;

import net.minecraft.server.permissions.PermissionLevel;

public interface PanelRequirements {
	boolean requireWorld();

	boolean requireWorld(String customMessage);

	boolean requireLevel(PermissionLevel level);

	boolean requireLevel(PermissionLevel level, String customMessage);

	boolean requireLevelOnServer(PermissionLevel level);

	boolean requireLevelOnServer(PermissionLevel level, String customMessage);

	boolean requireLocal();
}
