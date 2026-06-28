package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.ImGuiWindowType;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import org.jspecify.annotations.Nullable;

public abstract class Panel {
    private static final PanelRequirements REQUIREMENTS = new MinecraftPanelRequirements();
    private final Identifier id;
    private final String label;
    private final @Nullable ImIcon icon;
    private final ImGuiShortcut shortcut;
    private final PanelCategory category;
    private final boolean temporary;
    private final boolean menuBar;
    private final PanelStyle style;
    private final ImBoolean windowOpen = new ImBoolean(false);
    private boolean unsaved;
    private boolean focused;
    private boolean open;
    private ImGuiWindowType windowType = ImGuiWindowType.WINDOW;

    protected Panel(Builder builder) {
        this.id = builder.id;
        this.label = builder.label;
        this.icon = builder.icon;
        this.shortcut = builder.imGuiShortcut;
        this.category = builder.category;
        this.temporary = builder.temporary;
        this.menuBar = builder.menuBar;
        this.unsaved = builder.unsaved;
        this.style = builder.style;
    }

    protected static boolean requireWorld() {
        return REQUIREMENTS.requireWorld();
    }

    protected static boolean requireWorld(String message) {
        return REQUIREMENTS.requireWorld(message);
    }

    protected static boolean requireLevel(PermissionLevel level) {
        return REQUIREMENTS.requireLevel(level);
    }

    protected static boolean requireLevel(PermissionLevel level, String message) {
        return REQUIREMENTS.requireLevel(level, message);
    }

    protected static boolean requireLevelOnServer(PermissionLevel level) {
        return REQUIREMENTS.requireLevelOnServer(level);
    }

    protected static boolean requireLocal() {
        return REQUIREMENTS.requireLocal();
    }

    public abstract void content(ImGraphicsExtractor g);

    protected void onOpened() {
    }

    protected void onClosed() {
    }

    protected void onPreContent() {
    }

    protected void onPostContent() {
    }

    protected void onPreWindow() {
    }

    protected void tick() {
    }

    protected int customFlags() {
        return ImGuiWindowFlags.None;
    }

    public final Identifier getId() {
        return id;
    }

    public final String getLabel() {
        return label;
    }

    public final @Nullable ImIcon getIcon() {
        return icon;
    }

    public final ImGuiShortcut getShortcut() {
        return shortcut;
    }

    public final PanelCategory getCategory() {
        return category;
    }

    public final boolean isTemporary() {
        return temporary;
    }

    public final boolean hasMenuBar() {
        return menuBar;
    }

    public final boolean isUnsaved() {
        return unsaved;
    }

    protected final void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    public final PanelStyle getStyle() {
        return style;
    }

    public final boolean isOpen() {
        return open;
    }

    public final boolean isFocused() {
        return focused;
    }

    public final ImGuiWindowType getWindowType() {
        return windowType;
    }

    public final void open() {
        if (open) return;
        open = true;
        onOpened();
    }

    public final void close() {
        if (!open) return;
        open = false;
        onClosed();
    }

    public final boolean handleRender() {
        if (!open) return false;

        windowOpen.set(true);
        int flags = getFlags();

        if (style != PanelStyle.NORMAL && windowType != ImGuiWindowType.DOCKED) {
            flags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.AlwaysAutoResize;

            if (windowType == ImGuiWindowType.VIEWPORT && style == PanelStyle.TRANSPARENT) {
                flags |= ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDecoration;
            }

            ImGui.setNextWindowSizeConstraints(0F, 0F, 600F, Client.getWindow().getHeight() - 80F);
        } else {
            ImGui.setNextWindowSizeConstraints(160F, 90F, Float.MAX_VALUE, Float.MAX_VALUE);
        }

        onPreWindow();

        String title = getFormattedLabel() + "###" + this.id;
        boolean visible = ImGui.begin(title, windowOpen, flags);

        ImGraphicsExtractor g = Client.getImGraphics();
        g.pushStack();
        try {
            if (visible) {
                this.focused = ImGui.isWindowFocused();
                onPreContent();
                content(g);
                onPostContent();
            } else {
                this.focused = false;
            }

            windowType = ImGuiWindowType.get(Client.getWindow().handle());
        } finally {
            ImGui.end();
            g.popStack();
        }

        if (!windowOpen.get()) {
            close();
        }

        return open;
    }

    public final void handleTick() {
        tick();
    }

    protected final void menuBar(Runnable body) {
        if (ImGui.beginMenuBar()) {
            body.run();
            ImGui.endMenuBar();
        }
    }

    public String getFormattedLabel() {
        if (this.icon == null) {
            return this.label;
        }
        return this.label + " " + ImGraphicsExtractor.icon(this.icon);
    }

    private int getFlags() {
        int flags = ImGuiWindowFlags.None;

        if (unsaved) {
            flags |= ImGuiWindowFlags.UnsavedDocument;
        }

        if (temporary) {
            flags |= ImGuiWindowFlags.NoSavedSettings;
        }

        if (menuBar) {
            flags |= ImGuiWindowFlags.MenuBar;
        }

        flags |= customFlags();

        return flags;
    }

    public static final class Builder {
        private final Identifier id;
        private final String label;
        private @Nullable ImIcon icon;
        private ImGuiShortcut imGuiShortcut = ImGuiShortcut.empty();
        private PanelCategory category = PanelCategory.OPEN;
        private boolean temporary;
        private boolean menuBar;
        private boolean unsaved;
        private PanelStyle style = PanelStyle.NORMAL;

        public Builder(Identifier id, String label) {
            this.id = id;
            this.label = label;
        }

        public Builder icon(@Nullable ImIcon icon) {
            this.icon = icon;
            return this;
        }

        public Builder shortcut(ImGuiShortcut imGuiShortcut) {
            this.imGuiShortcut = imGuiShortcut;
            return this;
        }

        public Builder category(PanelCategory category) {
            this.category = category;
            return this;
        }

        public Builder temporary(boolean temporary) {
            this.temporary = temporary;
            return this;
        }

        public Builder menuBar(boolean menuBar) {
            this.menuBar = menuBar;
            return this;
        }

        public Builder unsaved(boolean unsaved) {
            this.unsaved = unsaved;
            return this;
        }

        public Builder style(PanelStyle style) {
            this.style = style;
            return this;
        }
    }
}
