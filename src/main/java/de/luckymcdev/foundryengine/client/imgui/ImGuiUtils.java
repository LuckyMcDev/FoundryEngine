package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;
import imgui.flag.*;
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
    private static final StringSplitter IM_GUI_SPLITTER = new StringSplitter((charId, style) -> Client.getImGuiManager().getFontManager().getCurrent().getCharAdvance(charId));

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

    /**
     * Displays an icon at 2x font scale.
     */
    public static void displayIcon(ImIcon icon) {
        ImGui.setWindowFontScale(2f);
        ImGui.text(icon.iconText(""));
        ImGui.setWindowFontScale(1f);
    }

    /**
     * Renders text at heading level 1 (3x font scale).
     */
    public static void h1(Runnable txt) {
        ImGui.setWindowFontScale(3f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    /**
     * Renders text at heading level 2 (2.5x font scale).
     */
    public static void h2(Runnable txt) {
        ImGui.setWindowFontScale(2.5f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    /**
     * Renders text at heading level 3 (2x font scale).
     */
    public static void h3(Runnable txt) {
        ImGui.setWindowFontScale(2f);
        txt.run();
        ImGui.setWindowFontScale(1f);
    }

    /**
     * Renders text at heading level 4 (1.5x font scale).
     */
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

    /**
     * Renders text in red if the condition is true.
     */
    public static void redTextIf(String text, boolean condition) {
        if (condition) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.2f, 0.2f, 1.0f);
        }
        ImGui.text(text);
        if (condition) {
            ImGui.popStyleColor();
        }
    }

    /**
     * Displays an identifier with a dimmed namespace
     *
     * @param loc The identifier
     */
    public static void identifier(Identifier loc) {
        ImGui.beginGroup();
        ImGui.textColored(colorOf(loc.getNamespace()).argb(), loc.getNamespace() + ":");

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
     * Formats a duration in milliseconds to a HH:MM:SS.mmm string.
     */
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
    public static Color colorOf(String modid) {
        if (modid == null) {
            return Color.WHITE;
        }
        int hash = modid.hashCode();
        return new Color(0xFF000000 | (hash & 0x00FFFFFF));
    }

    /**
     * @return A string splitter for ImGui fonts
     */
    public static StringSplitter getStringSplitter() {
        return IM_GUI_SPLITTER;
    }

    /**
     * Draws an {@link Image} record as an ImGui image.
     */
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

    /**
     * Draws an OpenGL texture as an ImGui image.
     */
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

    /**
     * Draws an OpenGL texture as a clickable ImGui image button.
     */
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

    /**
     * Loads a texture from an Identifier and returns its image record.
     */
    public static Image getTexture(Identifier texture) {
        return loadTexture("identifier", texture);
    }

    /**
     * Loads a texture from a file and returns its image record.
     */
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

    /**
     * Renders a section header with colored text and a separator below it.
     */
    public static void section(String title) {
        ImGui.textColored(0xFF00AAFF, title);
        ImGui.separator();
    }

    /**
     * Renders a scrollable child region at full available width/height with a border.
     */
    public static void scrollableRegion(String id, Runnable content) {
        scrollableRegion(id, 0, 0, true, content);
    }

    /**
     * Renders a scrollable child region with explicit dimensions.
     * Pass 0 for width or height to use the full available space.
     */
    public static void scrollableRegion(String id, float width, float height, boolean border, Runnable content) {
        if (ImGui.beginChild(id, width, height, border)) {
            content.run();
        }
        ImGui.endChild();
    }

    /**
     * Collapsible section header. Only runs {@code content} when the header is open.
     */
    public static boolean collapse(String label, Runnable content) {
        return collapse(label, ImGuiTreeNodeFlags.None, content);
    }

    /**
     * Collapsible section header with custom tree-node flags.
     */
    public static boolean collapse(String label, int flags, Runnable content) {
        if (ImGui.collapsingHeader(label, flags)) {
            content.run();
            return true;
        }
        return false;
    }

    /**
     * Renders a colored label followed by a dimmed value on the same line.
     */
    public static void labeledValue(String label, String value) {
        ImGui.textColored(0xFF00AAFF, label);
        ImGui.sameLine();
        ImGui.textDisabled(value);
    }

    /**
     * Runs {@code body} while the given font is pushed, then pops it.
     */
    public static void withFont(Identifier font, Runnable body) {
        var fonts = Client.getImGuiManager().getFontManager();
        fonts.pushFont(font);
        body.run();
        fonts.popFont();
    }

    /**
     * Runs {@code body} inside a framed, default-open tree node.
     */
    public static void treeSection(String label, Runnable body) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth
                | ImGuiTreeNodeFlags.DefaultOpen
                | ImGuiTreeNodeFlags.Framed;
        if (ImGui.treeNodeEx(label, flags, label)) {
            body.run();
            ImGui.treePop();
        }
    }

    /**
     * Renders a small icon button.
     */
    public static boolean iconButton(ImIcon icon, String id) {
        return ImGui.smallButton(icon(icon) + id);
    }

    /**
     * Returns a colored string formatted with the given arguments, using the
     * standard bright-blue tint for labels.
     */
    public static String formatColored(String text, Object... args) {
        return String.format(text, args);
    }


    /**
     * Dimmed, slightly spaced‑out uppercase label used to head a section.
     */
    public static void sectionLabel(String text) {
        ImGui.textDisabled(text);
        ImGui.spacing();
    }

    /**
     * Centered, dimmed placeholder message for empty states.
     */
    public static void centeredMessage(String text) {
        ImGui.dummy(0, 24);
        float avail = ImGui.getContentRegionAvailX();
        float textW = ImGui.calcTextSize(text).x;
        ImGui.setCursorPosX(Math.max(0, (avail - textW) / 2f));
        ImGui.textDisabled(text);
    }

    /**
     * Begins a bordered, padded "card" child region that auto-sizes to its content.
     */
    public static void cardBegin(String id) {
        ImGui.beginChild(id, 0, 0, true,
                ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoScrollbar);
    }

    /**
     * Ends a card region started with {@link #cardBegin(String)}.
     */
    public static void cardEnd() {
        ImGui.endChild();
    }

    /**
     * RGBA color picker that reads/writes an int[4] where each element is a packed ARGB int.
     * Returns true if the value changed.
     */
    public static boolean colorEdit4Int(String label, int[] rgba) {
        float[] f = new float[]{
                ((rgba[0] >> 16) & 0xFF) / 255f,
                ((rgba[0] >> 8) & 0xFF) / 255f,
                (rgba[0] & 0xFF) / 255f,
                ((rgba[0] >> 24) & 0xFF) / 255f
        };
        ImGui.setNextItemWidth(180);
        if (ImGui.colorEdit4(label, f, ImGuiColorEditFlags.NoInputs)) {
            int r = (int) (f[0] * 255);
            int g = (int) (f[1] * 255);
            int b = (int) (f[2] * 255);
            int a = (int) (f[3] * 255);
            rgba[0] = (a << 24) | (r << 16) | (g << 8) | b;
            return true;
        }
        return false;
    }

    public record Image(int glId, int width, int height) {
    }
}
