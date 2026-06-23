package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStackTemplate;

import java.io.File;
import java.net.URI;

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

    private static final ComponentCharSink COMPONENT_SINK = new ComponentCharSink();

    /**
     * Draws a texture previously resolved into an {@link ImGuiTexture} at its native size.
     */
    public static void drawImage(ImGuiTexture texture) {
        texture.draw();
    }

    /**
     * Draws a texture previously resolved into an {@link ImGuiTexture}, scaled to the given size.
     */
    public static void drawImage(ImGuiTexture texture, float w, float h) {
        texture.draw(w, h);
    }

    /**
     * Draws a clickable image button for a texture previously resolved into an {@link ImGuiTexture}.
     *
     * @return true if the button was clicked this frame
     */
    public static boolean drawImageButton(ImGuiTexture texture, float w, float h) {
        return texture.drawButton(w, h);
    }

    /**
     * Resolves the texture currently registered to {@code textureId} in Minecraft's
     * texture manager into a drawable {@link ImGuiTexture}.
     */
    public static ImGuiTexture getTexture(Identifier textureId) {
        return ImGuiTexture.of(textureId);
    }

    /**
     * Loads an image file from disk into a drawable {@link ImGuiTexture}.
     */
    public static ImGuiTexture getTexture(File imageFile) {
        return ImGuiTexture.of(imageFile);
    }

    /**
     * Resolves and draws the texture registered to {@code textureId} at the given size.
     */
    public static void image(Identifier textureId, float w, float h) {
        ImGuiTexture.of(textureId).draw(w, h);
    }

    /**
     * Draws an already-resolved Minecraft texture at the given size.
     */
    public static void image(AbstractTexture texture, float w, float h) {
        ImGuiTexture.of(texture).draw(w, h);
    }

    /**
     * Resolves the texture registered to {@code textureId} and draws it as a clickable button.
     *
     * @return true if the button was clicked this frame
     */
    public static boolean imageButton(Identifier textureId, float w, float h) {
        return ImGuiTexture.of(textureId).drawButton(w, h);
    }

    /**
     * Draws an already-resolved Minecraft texture as a clickable button.
     *
     * @return true if the button was clicked this frame
     */
    public static boolean imageButton(AbstractTexture texture, float w, float h) {
        return ImGuiTexture.of(texture).drawButton(w, h);
    }

    public static void component(Component text) {
        component(text, Float.MAX_VALUE);
    }

    public static void component(Component text, float wrapWidth) {
        var sink = COMPONENT_SINK;
        sink.setup();
        var lines = IM_GUI_SPLITTER.splitLines(text, Math.max((int) wrapWidth, 1), Style.EMPTY);
        for (var part : Language.getInstance().getVisualOrder(lines)) {
            part.accept(sink);
            sink.finish();
            ImGui.newLine();
        }
        sink.reset();
    }

    private static final class ComponentCharSink implements FormattedCharSink {
        private final StringBuilder buffer = new StringBuilder();
        private ImFont font;
        private int textColor;
        private HoverEvent hoverEvent;
        private ClickEvent clickEvent;

        void setup() {
            font = ImGui.getFont();
            textColor = ImGui.getColorU32(ImGuiCol.Text);
        }

        void reset() {
            font = null;
            textColor = 0;
            buffer.setLength(0);
            hoverEvent = null;
            clickEvent = null;
        }

        @Override
        public boolean accept(int index, Style style, int codePoint) {
            var styleFont = resolveFont(style);
            int styleColor = style.getColor() != null ? style.getColor().getValue() : textColor;
            if (styleFont != font || styleColor != textColor || style.getHoverEvent() != hoverEvent || style.getClickEvent() != clickEvent) {
                if (!buffer.isEmpty()) finish();
                font = styleFont;
                textColor = styleColor;
                hoverEvent = style.getHoverEvent();
                clickEvent = style.getClickEvent();
            }
            buffer.appendCodePoint(codePoint);
            return true;
        }

        void finish() {
            if (buffer.isEmpty()) return;

            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
            ImGui.pushFont(font);
            int argb = 0xFF000000
                    | (textColor & 0xFF0000) >> 16
                    | (textColor & 0xFF00)
                    | (textColor & 0xFF) << 16;
            ImGui.textColored(argb, buffer.toString());
            ImGui.popStyleVar();
            buffer.setLength(0);

            if (ImGui.isItemClicked() && clickEvent != null) handleClick();
            if (ImGui.isItemHovered() && hoverEvent != null) {
                if (clickEvent != null) ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
                handleHover();
            }

            ImGui.sameLine();
            ImGui.popFont();
        }

        private ImFont resolveFont(Style style) {
            var fonts = Client.getImGuiManager().getFontManager();
            var fontDesc = style.getFont();
            if (fontDesc != null) {
                if (fontDesc instanceof FontDescription.Resource(var fontId)) {
                    try {
                        return fonts.getFont(fontId);
                    } catch (Exception ignored) {}
                }
            }
            if (style.isBold() && style.isItalic()) return fonts.getFont(net.minecraft.resources.Identifier.withDefaultNamespace("bold_italic"));
            if (style.isBold()) return fonts.getFont(net.minecraft.resources.Identifier.withDefaultNamespace("bold"));
            if (style.isItalic()) return fonts.getFont(net.minecraft.resources.Identifier.withDefaultNamespace("italic"));
            return ImGui.getFont();
        }

        private void handleClick() {
            var mc = Minecraft.getInstance();
            switch (clickEvent) {
                case ClickEvent.OpenUrl(URI uri):
                    if (!mc.options.chatLinks().get()) return;
                    if (mc.options.chatLinksPrompt().get()) {
                        var oldScreen = mc.screen;
                        mc.setScreen(new ConfirmLinkScreen(confirm -> {
                            if (confirm) Util.getPlatform().openUri(uri);
                            mc.setScreen(oldScreen);
                        }, uri.toString(), false));
                    } else {
                        Util.getPlatform().openUri(uri);
                    }
                    break;
                case ClickEvent.OpenFile(String path):
                    Util.getPlatform().openUri(new java.io.File(path).toURI());
                    break;
                case ClickEvent.RunCommand(String cmd):
                    var player = mc.player;
                    if (player != null) {
                        player.connection.sendUnattendedCommand(
                                net.minecraft.commands.Commands.trimOptionalPrefix(cmd), mc.screen);
                    }
                    break;
                case ClickEvent.CopyToClipboard(String text):
                    mc.keyboardHandler.setClipboard(text);
                    break;
                default:
                    break;
            }
        }

        private void handleHover() {
            var mc = Minecraft.getInstance();
            switch (hoverEvent) {
                case HoverEvent.ShowItem(ItemStackTemplate item):
                    var tooltip = Screen.getTooltipFromItem(mc, item.create());
                    ImGui.beginTooltip();
                    for (var line : tooltip) component(line, ImGui.getFontSize() * 35.0f);
                    ImGui.endTooltip();
                    break;
                case HoverEvent.ShowEntity(HoverEvent.EntityTooltipInfo info):
                    if (mc.options.advancedItemTooltips) {
                        ImGui.beginTooltip();
                        for (var line : info.getTooltipLines()) component(line, ImGui.getFontSize() * 35.0f);
                        ImGui.endTooltip();
                    }
                    break;
                case HoverEvent.ShowText(Component component):
                    ImGui.beginTooltip();
                    component(component, ImGui.getFontSize() * 35.0f);
                    ImGui.endTooltip();
                    break;
                default:
                    break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layout / structural helpers
    // ─────────────────────────────────────────────────────────────────────────

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
}