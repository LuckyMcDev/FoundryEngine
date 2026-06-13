package de.luckymcdev.foundryengine.client.editor.panel;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.ImGuiWindowType;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;

/**
 * An ImGui Panel.
 */
public class Panel {
    /**
     * The Id.
     */
    public final Identifier id;
    /**
     * The Label.
     */
    public final String label;
    /**
     * Icon which is displayed next to the label.
     */
    public final @Nullable ImIcon icon;
    /**
     * The Panels opening Shortcut.
     */
    public final Shortcut shortcut;
    /**
     * If the Panel is Temporary.
     */
    public boolean temporary;
    /**
     * If the Panel has a Menu Bar.
     */
    public boolean menuBar;
    /**
     * If the Panel is unsaved.
     */
    public boolean unsaved;
    /**
     * The Panels Style.
     */
    public PanelStyle style;
    /**
     * If the Panel is Open.
     */
    public boolean open;
    /**
     * The Panels Type.
     */
    public ImGuiWindowType type;

    public PanelCategory category;

    private boolean focused;

    /**
     * Instantiates a new Panel.
     *
     * @param id       the id
     * @param label    the label
     * @param shortcut the Shortcut.
     */
    protected Panel(Identifier id, String label, ImIcon icon, Shortcut shortcut) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.shortcut = shortcut;
        this.open = false;
        this.temporary = false;
        this.menuBar = false;
        this.style = PanelStyle.NORMAL;
        this.type = ImGuiWindowType.WINDOW;
        this.category = PanelCategory.OPEN;
    }

    protected Panel(Identifier id, String label, ImIcon icon) {
        this(id, label, icon, Shortcut.empty());
    }

    /**
     * Instantiates a new Panel.
     *
     * @param id    the id
     * @param label the label
     */
    protected Panel(Identifier id, String label) {
        this(id, label, null, Shortcut.empty());
    }

    protected Panel(Identifier id, String label, ImIcon icon, Shortcut shortcut, PanelCategory category) {
        this(id, label, icon, shortcut);
        this.category = category;
    }

    protected Panel(Identifier id, String label, ImIcon icon, PanelCategory category) {
        this(id, label, icon);
        this.category = category;
    }

    protected Panel(Identifier id, String label, PanelCategory category) {
        this(id, label);
        this.category = category;
    }

    /**
     * Delegates to {@link PanelRequirements#requireWorld()}.
     */
    protected static boolean requireWorld() {
        return PanelRequirements.requireWorld();
    }

    /**
     * Delegates to {@link PanelRequirements#requireWorld(String)}.
     */
    protected static boolean requireWorld(String message) {
        return PanelRequirements.requireWorld(message);
    }

    /**
     * Delegates to {@link PanelRequirements#requireLevel(PermissionLevel)}.
     */
    protected static boolean requireLevel(PermissionLevel level) {
        return PanelRequirements.requireLevel(level);
    }

    /**
     * Delegates to {@link PanelRequirements#requireLevel(PermissionLevel, String)}.
     */
    protected static boolean requireLevel(PermissionLevel level, String message) {
        return PanelRequirements.requireLevel(level, message);
    }

    /**
     * Delegates to {@link PanelRequirements#requireLevelOnServer(PermissionLevel)}.
     */
    protected static boolean requireLevelOnServer(PermissionLevel level) {
        return PanelRequirements.requireLevelOnServer(level);
    }

    /**
     * Delegates to {@link PanelRequirements#requireLocal()}.
     */
    protected static boolean requireLocal() {
        return PanelRequirements.requireLocal();
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public Identifier getId() {
        return this.id;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
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

    /**
     * Gets Shortcut.
     *
     * @return the shortcut
     */
    public Shortcut getShortcut() {
        return shortcut;
    }

    /**
     * Opens the Panel.
     */
    public final void open() {
        if (!this.open) {
            this.open = true;
        }
        onOpened();
    }

    /**
     * Closes the Panel.
     */
    public final void close() {
        if (this.open) {
            this.open = false;
        }
    }

    /**
     * On opened.
     * Override for custom functionality.
     */
    public void onOpened() {
    }

    /**
     * On closed.
     * Override for custom functionality.
     */
    public void onClosed() {
    }

    /**
     * Handles rendering of the Panel.
     *
     * @return if the window is open
     */
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

    /**
     * Is focused boolean.
     *
     * @return focused
     */
    public boolean isFocused() {
        return this.focused;
    }

    /**
     * Is open boolean.
     *
     * @return open
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Handle tick.
     */
    public final void handleTick() {
        tick();
    }

    /**
     * Content.
     * Overwrite for custom Functionality
     */
    public void content() {
    }

    /**
     * Tick.
     * Overwrite for custom Functionality
     */
    public void tick() {
    }

    /**
     * Wraps {@link imgui.ImGui#beginMenuBar()} / {@link imgui.ImGui#endMenuBar()}.
     */
    protected void menuBar(Runnable body) {
        if (ImGui.beginMenuBar()) {
            body.run();
            ImGui.endMenuBar();
        }
    }
}