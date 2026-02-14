package io.github.luckymcdev.client.editor;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import io.github.luckymcdev.client.editor.config.ImGuiWindowType;
import io.github.luckymcdev.client.editor.config.PanelStyle;
import io.github.luckymcdev.common.Instances;
import net.minecraft.resources.ResourceLocation;

/**
 * An ImGui Panel.
 */
public class Panel {
    private boolean focused;
    /**
     * The Id.
     */
    public ResourceLocation id;
    /**
     * The Label.
     */
    public String label;
    /**
     * If the Panel is Open.
     */
    public boolean open;
    /**
     * If the Panel is Temporary.
     */
    public boolean temporary;
    /**
     * The Panels Style.
     */
    public PanelStyle style;
    /**
     * The Panels Type.
     */
    public ImGuiWindowType type;

    /**
     * Instantiates a new Panel.
     *
     * @param id    the id
     * @param label the label
     */
    protected Panel(ResourceLocation id, String label) {
        this.id = id;
        this.label = label;
        this.open = false;
        this.temporary = false;
        this.style = PanelStyle.NORMAL;
        this.type = ImGuiWindowType.WINDOW;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public ResourceLocation getId() {
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

            ImGui.setNextWindowSizeConstraints(0F, 0F, 600F, Instances.getWindow().getHeight() - 80F);
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

        type = ImGuiWindowType.get(Instances.getWindow().handle());
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
