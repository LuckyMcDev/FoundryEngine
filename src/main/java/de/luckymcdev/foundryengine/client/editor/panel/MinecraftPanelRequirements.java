package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.ImVec4;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class MinecraftPanelRequirements implements PanelRequirements {
    private static final ImVec4 DENIED_COLOR = new ImVec4(1.0f, 0.5f, 0.0f, 1.0f);

    private static void textDenied(String title, String... lines) {
        ImGui.textColored(DENIED_COLOR,
                ImGuiUtils.icon(ImIcons.FA.FA_EXCLAMATION_TRIANGLE) + " " + title);
        ImGui.spacing();
        for (String line : lines) {
            ImGui.textDisabled(line);
        }
    }

    @Override
    public boolean requireWorld() {
        return requireWorld("You need to join a world for this panel to work.");
    }

    @Override
    public boolean requireWorld(String customMessage) {
        if (Minecraft.getInstance().level == null) {
            textDenied("World Required", customMessage);
            return false;
        }
        return true;
    }

    @Override
    public boolean requireLevel(PermissionLevel level) {
        return requireLevel(level, "You need " + level.name().toLowerCase() + " permissions to use this panel.");
    }

    @Override
    public boolean requireLevel(PermissionLevel level, String customMessage) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            textDenied("World Required", "You need to join a world for this panel to work.");
            return false;
        }
        if (!mc.isSingleplayer()) {
            if (!Client.getPlayer().permissions().hasPermission(new Permission.HasCommandLevel(level))) {
                textDenied("Insufficient permissions", customMessage);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requireLevelOnServer(PermissionLevel level) {
        return requireLevelOnServer(level, "You need " + level.name().toLowerCase() + " permissions to use this panel.");
    }

    @Override
    public boolean requireLevelOnServer(PermissionLevel level, String customMessage) {
        var mc = Minecraft.getInstance();
        if (mc.level != null && !mc.isSingleplayer()) {
            if (mc.player == null) {
                textDenied("Insufficient permissions", customMessage);
                return false;
            }
            if (!Client.getPlayer().permissions().hasPermission(new Permission.HasCommandLevel(level))) {
                textDenied("Insufficient permissions", customMessage);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requireLocal() {
        var mc = Minecraft.getInstance();
        if (mc.level != null && !mc.isSingleplayer()) {
            textDenied("Not Available",
                    "This panel is not available while connected to a server.");
            return false;
        }
        return true;
    }
}
