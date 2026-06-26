package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.ImGuiWindowType;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;

public class Panel {
    private static PanelRequirements defaultRequirements = new MinecraftPanelRequirements();
    public final Identifier id;
    public final String label;
    public final @Nullable ImIcon icon;
    public final ImGuiShortcut imGuiShortcut;
    public boolean temporary;
    public boolean menuBar;
    public boolean unsaved;
    public PanelStyle style;
    public boolean open;
    public ImGuiWindowType type;
    public PanelCategory category;
    private boolean focused;

    protected Panel(Builder builder) {
        this.id = builder.id;
        this.label = builder.label;
        this.icon = builder.icon;
        this.imGuiShortcut = builder.imGuiShortcut;
        this.category = builder.category;
        this.temporary = builder.temporary;
        this.menuBar = builder.menuBar;
        this.unsaved = builder.unsaved;
        this.style = builder.style;
        this.open = false;
        this.type = ImGuiWindowType.WINDOW;
    }

    public static void setDefaultRequirements(PanelRequirements requirements) {
        defaultRequirements = requirements;
    }

    protected static boolean requireWorld() {
        return defaultRequirements.requireWorld();
    }

    protected static boolean requireWorld(String message) {
        return defaultRequirements.requireWorld(message);
    }

    protected static boolean requireLevel(PermissionLevel level) {
        return defaultRequirements.requireLevel(level);
    }

    protected static boolean requireLevel(PermissionLevel level, String message) {
        return defaultRequirements.requireLevel(level, message);
    }

    protected static boolean requireLevelOnServer(PermissionLevel level) {
        return defaultRequirements.requireLevelOnServer(level);
    }

    protected static boolean requireLocal() {
        return defaultRequirements.requireLocal();
    }

    public Identifier getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public String getFormattedLabel() {
        if (this.icon == null) {
            return this.label;
        } else {
            return this.label + " " + ImGuiUtils.icon(this.icon);
        }
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

    public int customFlags() {
        return ImGuiWindowFlags.None;
    }

    public ImGuiShortcut getShortcut() {
        return imGuiShortcut;
    }

    public final void open() {
        if (!this.open) {
            this.open = true;
        }
        onOpened();
    }

    public final void close() {
        if (this.open) {
            this.open = false;
        }
    }

    public void onOpened() {
    }

    public void onClosed() {
    }

    public final boolean handleRender() {
        int flags = getFlags();
        ImBoolean WINDOW = new ImBoolean(this.open);

        if (this.style != PanelStyle.NORMAL && this.type != ImGuiWindowType.DOCKED) {
            flags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.AlwaysAutoResize;

            if (this.type == ImGuiWindowType.VIEWPORT && this.style == PanelStyle.TRANSPARENT) {
                flags |= ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDecoration;
            }

            ImGui.setNextWindowSizeConstraints(0F, 0F, 600F, Client.getWindow().getHeight() - 80F);
        } else {
            ImGui.setNextWindowSizeConstraints(160F, 90F, Float.MAX_VALUE, Float.MAX_VALUE);
        }

        String title = this.getFormattedLabel() + "###" + this.getId();

        boolean menuOpen = ImGui.begin(title, WINDOW, flags);

        try {
            if (menuOpen) {
                boolean shouldClose = !WINDOW.get();
                this.focused = ImGui.isWindowFocused();

                this.content();

                if (shouldClose) {
                    this.close();
                }

                if (!open) {
                    onClosed();
                }
            } else {
                this.focused = false;
                if (!WINDOW.get()) {
                    this.close();
                    if (!open) {
                        onClosed();
                    }
                }
            }

            type = ImGuiWindowType.get(Client.getWindow().handle());
        } finally {
            ImGui.end();
        }

        return open;
    }

    public boolean isFocused() {
        return this.focused;
    }

    public boolean isOpen() {
        return this.open;
    }

    public final void handleTick() {
        tick();
    }

    public void content() {
    }

    public void tick() {
    }

    protected void menuBar(Runnable body) {
        if (ImGui.beginMenuBar()) {
            body.run();
            ImGui.endMenuBar();
        }
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
