package io.github.luckymcdev.client.editor;

import com.mojang.blaze3d.systems.RenderPass;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.internal.ImGuiWindow;
import imgui.type.ImBoolean;
import io.github.luckymcdev.client.editor.config.ImGuiWindowType;
import io.github.luckymcdev.client.editor.config.PanelStyle;
import io.github.luckymcdev.common.Instances;
import net.minecraft.resources.ResourceLocation;

public class Panel {
    public ResourceLocation id;
    public String label;
    public boolean open;
    public boolean temporary;
    public PanelStyle style;
    public ImGuiWindowType type;


    protected Panel(ResourceLocation id, String label) {
        this.id = id;
        this.label = label;
        this.open = false;
        this.temporary = false;
        this.style = PanelStyle.NORMAL;
        this.type = ImGuiWindowType.WINDOW;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    private int getFlags() {
        int flags = ImGuiWindowFlags.None;

        if(temporary) {
            flags |= ImGuiWindowFlags.NoSavedSettings;
        }

        return flags;
    }

    public final void open() {
        if(!this.open) {
            this.open = true;
        }
        onOpened();
    }

    public final void close() {
        if(this.open) {
            this.open = false;
        }
    }

    public void onOpened() {
    }

    public void onClosed() {
    }

    public final boolean handle() {
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

        if(menuOpen) {
            boolean shouldClose = !WINDOW.get();

            this.content();

            if(shouldClose) {
                this.close();
            }

            if (!open) {
                onClosed();
            }
        }

        type = ImGuiWindowType.get(Instances.getWindow().handle());
        ImGui.end();

        return open;
    }

    public void content() {

    }

    public void tick() {

    }

}
