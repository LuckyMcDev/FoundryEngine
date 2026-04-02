package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.resources.Identifier;

/**
 * A Class which has static methods for
 * some ImGui utils.
 */
public class ImGuiUtils {
    private static final StringSplitter IM_GUI_SPLITTER = new StringSplitter((charId, style) -> Client.getImGuiManager().getFont().getCharAdvance(charId));

    /**
     * Displays a (?) with a hover tooltip. Useful for example information.
     * It is rendered at the same line as the thing before it.
     *
     * @param text The tooltip text
     */
    public static void helpTooltip(String text) {
        ImGui.sameLine();
        ImGui.textColored(0xFF555555, "(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textColored(0xFFFFFFFF, text);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }
    }

    /**
     * Returns an icon
     *
     * @param icon The icon
     */
    public static String icon(ImIcon icon) {
        return "" + icon;
    }

    public static void displayIcon(ImIcon icon) {
        ImGui.setWindowFontScale(2f);
        ImGui.text(icon.iconText(""));
        ImGui.setWindowFontScale(1f);
    }

    public static void h1(Runnable txt) {
        ImGui.setWindowFontScale(3f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    public static void h2(Runnable txt) {
        ImGui.setWindowFontScale(2.5f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    public static void h3(Runnable txt) {
        ImGui.setWindowFontScale(2f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    public static void h4(Runnable txt) {
        ImGui.setWindowFontScale(1.5f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    /**
     * Helper to draw centered text.
     *
     * @param text  The text to render
     * @param width The width of the area to center on
     */
    public static void textCentered(String text, float width) {
        ImGui.setCursorPosX(ImGui.getCursorPosX() + (width - ImGui.getFont().calcTextSizeAX(ImGui.getFontSize(), Float.MAX_VALUE, 0, text)) / 2);
        ImGui.text(text);
    }

    public static void textDenied(String title, String... lines) {
        ImGui.textColored(new ImVec4(1.0f, 0.5f, 0.0f, 1.0f),
                ImGuiUtils.icon(ImIcons.FA.FA_EXCLAMATION_TRIANGLE) + " " + title);
        ImGui.spacing();
        for (String line : lines) {
            ImGui.textDisabled(line);
        }
    }

    public static boolean requiresActiveSession() {
        var mc = Minecraft.getInstance();
        return mc.level != null && !mc.isSingleplayer();
    }

    public static boolean requirePermissions() {
        var player = Client.getPlayer();
        if (player == null) return false;
        if (!PermissionChecks.COMMANDS_OWNER.check(player.permissions())) {
            textDenied("Insufficient permissions",
                    "You need owner-level permissions to use this panel.");
            return false;
        }
        return true;
    }

    public static boolean requireFull() {
        if (!requiresActiveSession()) return true; // not on a server, allow
        return requirePermissions();               // on a server, check perms
    }

    /**
     * Displays a resource location with a dimmed namespace
     *
     * @param loc The resource location
     */
    public static void resourceLocation(Identifier loc) {
        ImGui.beginGroup();
        ImGui.textColored(colorOf(loc.getNamespace()), loc.getNamespace() + ":");

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.sameLine();
        ImGui.text(loc.getPath());
        ImGui.popStyleVar();

        ImGui.endGroup();

        if (ImGui.beginPopupContextItem("" + loc)) {
            if (ImGui.selectable("##copy_location")) {
                ImGui.setClipboardText(loc.toString());
            }

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
            ImGui.setItemAllowOverlap();
            ImGui.sameLine();
            ImGuiUtils.icon(ImIcons.FA.FA_CLIPBOARD);
            ImGui.sameLine();
            ImGui.popStyleVar();
            ImGui.text("Copy Location");
            ImGui.endPopup();
        }
    }

    /**
     * Obtains the color of the modid
     *
     * @param modid The modid to get the color of
     * @return color The color based on the hash of the modid
     */
    public static int colorOf(String modid) {
        if (modid == null) {
            return Color.WHITE.argb();
        }
        int hash = modid.hashCode();
        return 0xFF000000 | (hash & 0x00FFFFFF);
    }

    /**
     * @return A string splitter for ImGui fonts
     */
    public static StringSplitter getStringSplitter() {
        return IM_GUI_SPLITTER;
    }
}
