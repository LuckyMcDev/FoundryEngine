package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.PermissionChecks;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
     * Pushes a red style for buttons to indicate a dangerous or error-related action.
     * Must be paired with {@link #popErrorButtonStyle()}.
     */
    public static void pushErrorButtonStyle() {
        ImGui.pushStyleColor(ImGuiCol.Button, 0.6f, 0.1f, 0.1f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.8f, 0.2f, 0.2f, 1.0f);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.4f, 0.05f, 0.05f, 1.0f);
    }

    /**
     * Pops the 3 style colors pushed by {@link #pushErrorButtonStyle()}.
     */
    public static void popErrorButtonStyle() {
        ImGui.popStyleColor(3);
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

    /**
     * Helper to draw something centered
     *
     * @param runnable   What to draw
     * @param itemWidth  The known width of the item(s) you are drawing
     * @param totalWidth The width of the area to center within (usually ImGui.getContentRegionAvailX())
     */
    public static void centered(Runnable runnable, float itemWidth, float totalWidth) {
        float posX = ImGui.getCursorPosX() + (totalWidth - itemWidth) / 2f;
        ImGui.setCursorPosX(posX);
        runnable.run();
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

    /**
     * Checks if the current environment meets world/level requirements for panel functionality.
     * Displays appropriate error message if requirements are not met.
     *
     * @param requireSingleplayer if true, requires singleplayer mode
     * @param requireMultiplayer  if true, requires multiplayer mode
     * @param customMessage       custom message to display if requirements not met (null for default)
     * @return true if requirements are met, false otherwise
     */
    public static boolean requireWorld(boolean requireSingleplayer, boolean requireMultiplayer, String customMessage) {
        var mc = Minecraft.getInstance();

        if (mc.level == null) {
            if (customMessage != null) {
                textDenied("World Required", customMessage);
            } else {
                textDenied("World Required", "You need to join a world for this panel to work.");
            }
            return false;
        }

        if (requireSingleplayer && !mc.isSingleplayer()) {
            textDenied("Singleplayer Required", "This panel only works in singleplayer mode.");
            return false;
        }

        if (requireMultiplayer && mc.isSingleplayer()) {
            textDenied("Multiplayer Required", "This panel only works in multiplayer mode.");
            return false;
        }

        return true;
    }

    /**
     * Convenience method for panels that require any world (singleplayer or multiplayer).
     *
     * @param customMessage custom message to display if no world is available
     * @return true if in a world, false otherwise
     */
    public static boolean requireWorld(String customMessage) {
        return requireWorld(false, false, customMessage);
    }

    /**
     * Convenience method for panels that require any world with default message.
     *
     * @return true if in a world, false otherwise
     */
    public static boolean requireWorld() {
        return requireWorld(false, false, null);
    }

    /**
     * Checks if a player entity is available (non-null).
     *
     * @param customMessage custom message to display if player is not available
     * @return true if player is available, false otherwise
     */
    public static boolean requirePlayer(String customMessage) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            if (customMessage != null) {
                textDenied("Player Required", customMessage);
            } else {
                textDenied("Player Required", "You need to be logged in for this panel to work.");
            }
            return false;
        }
        return true;
    }

    /**
     * Convenience method for requiring player with default message.
     *
     * @return true if player is available, false otherwise
     */
    public static boolean requirePlayer() {
        return requirePlayer(null);
    }

    /**
     * Combined check for both world and player requirements.
     *
     * @param customMessage custom message to display if requirements not met
     * @return true if both world and player are available, false otherwise
     */
    public static boolean requireWorldAndPlayer(String customMessage) {
        var mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            if (customMessage != null) {
                textDenied("World and Player Required", customMessage);
            } else {
                textDenied("World and Player Required", "You need to join a world and be logged in.");
            }
            return false;
        }
        return true;
    }

    /**
     * Convenience method for requiring both world and player with default message.
     *
     * @return true if both world and player are available, false otherwise
     */
    public static boolean requireWorldAndPlayer() {
        return requireWorldAndPlayer(null);
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

    public static String timer(long time) {
        long ms = time % 1000;
        long sec = (time / 1000) % 60;
        long min = (time / (1000 * 60)) % 60;
        long hrs = (time / (1000 * 60 * 60));

        StringBuilder sb = new StringBuilder();

        if (hrs > 0) {
            if (hrs < 10) sb.append('0');
            sb.append(hrs).append(':');
        }

        if (min < 10) sb.append('0');
        sb.append(min).append(':');

        if (sec < 10) sb.append('0');
        sb.append(sec).append('.');

        if (ms < 100) sb.append('0');
        if (ms < 10) sb.append('0');
        sb.append(ms);

        return sb.toString();
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

    public static void drawImage(Image image) {
        GlStateManager._bindTexture(image.glId());

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        var stack = Client.getImGuiManager().getGraphicsStack();
        stack.push();
        stack.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);

        ImGui.image(image.glId(), image.width(), image.height(), 0, 0, 1, 1);
        stack.pop();
    }

    public static void drawImage(int id, float w, float h) {
        GlStateManager._bindTexture(id);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        var stack = Client.getImGuiManager().getGraphicsStack();
        stack.push();
        stack.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);

        ImGui.image(id, w, h, 0, 0, 1, 1);
        stack.pop();
    }

    public static void drawImageButton(int id, float w, float h) {
        GlStateManager._bindTexture(id);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        var stack = Client.getImGuiManager().getGraphicsStack();
        stack.push();
        stack.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);

        ImGui.imageButton(id, w, h, 0, 0, 1, 1);
        stack.pop();
    }

    public static Image getTexture(Identifier texture) {
        return loadTexture("identifier", texture);
    }

    public static Image getTexture(File imageFile) {
        return loadTexture("file", imageFile);
    }

    private static <T> Image loadTexture(String type, T idOrFile) {
        int textureId = -1;
        int width = -1;
        int height = -1;
        if (type.equals("identifier")) {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture((Identifier) idOrFile);
            GpuTextureView textureView = texture.getTextureView();
            if (texture != null) {
                GlTexture glTexture = (GlTexture) texture.getTexture();
                textureId = glTexture.glId();
                width = textureView.getWidth(textureView.baseMipLevel());
                height = textureView.getHeight(textureView.baseMipLevel());
            }
        } else {
            File file = (File) idOrFile;
            try (InputStream is = new FileInputStream(file)) {
                NativeImage image = NativeImage.read(is);
                DynamicTexture texture = new DynamicTexture(() -> "EditorView_" + file.getName(), image);
                GpuTextureView textureView = texture.getTextureView();
                if (texture != null) {
                    GlTexture glTexture = (GlTexture) texture.getTexture();
                    textureId = glTexture.glId();
                    width = textureView.getWidth(textureView.baseMipLevel());
                    height = textureView.getHeight(textureView.baseMipLevel());
                }
            } catch (IOException e) {
                Common.LOGGER.error("Failed to load texture for viewer: {}", file.getAbsolutePath(), e);
            }
        }
        return new Image(textureId, width, height);
    }

    public record Image(int glId, int width, int height) {
    }
}
