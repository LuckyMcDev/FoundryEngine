package io.github.luckymcdev.foundryengine.client.editor;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.config.ImGuiWindowType;
import io.github.luckymcdev.foundryengine.client.editor.config.PanelStyle;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import net.minecraft.resources.Identifier;

/**
 * An ImGui Panel.
 */
public class Panel {
    /**
     * The Id.
     */
    private final Identifier id;
    /**
     * The Label.
     */
    private final String label;
    /**
     * If the Panel is Temporary.
     */
    private final boolean temporary;
    /**
     * The Panels Style.
     */
    private final PanelStyle style;
    private final Shortcut shortcut;
    /**
     * If the Panel is Open.
     */
    private boolean open;
    /**
     * The Panels Type.
     */
    private ImGuiWindowType type;

    private boolean focused;

    /**
     * Instantiates a new Panel.
     *
     * @param id    the id
     * @param label the label
     * @param shortcut the Shortcut.
     */
    protected Panel(Identifier id, String label, Shortcut shortcut) {
        this.id = id;
        this.label = label;
        this.shortcut = shortcut;
        this.open = false;
        this.temporary = false;
        this.style = PanelStyle.NORMAL;
        this.type = ImGuiWindowType.WINDOW;
    }

    /**
     * Instantiates a new Panel.
     *
     * @param id    the id
     * @param label the label
     */
    protected Panel(Identifier id, String label) {
        this(id, label, null);
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

    private int getFlags() {
        int flags = ImGuiWindowFlags.None;

        if (temporary) {
            flags |= ImGuiWindowFlags.NoSavedSettings;
        }

        return flags;
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
        var title = this.getLabel() + "###" + this.getId().toString();

        boolean menuOpen = ImGui.begin(title, WINDOW, flags);

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
        }

        type = ImGuiWindowType.get(Client.getWindow().handle());
        ImGui.end();

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

}