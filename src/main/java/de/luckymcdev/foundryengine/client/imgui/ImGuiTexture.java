package de.luckymcdev.foundryengine.client.imgui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A GPU texture that has been (or will be) registered with the {@link ImGuiRenderer}
 * so it can be drawn inside ImGui.
 */
public final class ImGuiTexture {
    private static final ImGuiTexture MISSING = new ImGuiTexture(null, 0, 0);
    private final @Nullable GpuTextureView view;
    private final int width;
    private final int height;
    private long imguiId = -1;

    private ImGuiTexture(@Nullable GpuTextureView view, int width, int height) {
        this.view = view;
        this.width = width;
        this.height = height;
    }

    /**
     * Wraps an already-resolved {@link GpuTextureView}.
     */
    public static ImGuiTexture of(GpuTextureView view) {
        if (view == null) return MISSING;
        int w = view.getWidth(view.baseMipLevel());
        int h = view.getHeight(view.baseMipLevel());
        return new ImGuiTexture(view, w, h);
    }

    /**
     * Resolves the texture currently bound to a Minecraft {@link Identifier} in the
     * texture manager (e.g. a block atlas sprite, a registered resource-pack texture).
     */
    public static ImGuiTexture of(Identifier textureId) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureId);
        return of(texture);
    }

    /**
     * Wraps an existing Minecraft {@link AbstractTexture}.
     */
    public static ImGuiTexture of(AbstractTexture texture) {
        if (texture == null) return MISSING;
        return of(texture.getTextureView());
    }

    /**
     * Loads an image file from disk into a brand-new {@link DynamicTexture} and wraps it.
     * The returned texture owns the underlying GPU resource; callers that load many
     * one-off images (e.g. an asset browser) are responsible for not leaking them if
     * the source files change.
     */
    public static ImGuiTexture of(File imageFile) {
        try (InputStream is = new FileInputStream(imageFile)) {
            NativeImage nativeImage = NativeImage.read(is);
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "ImGuiTexture_" + imageFile.getName(), nativeImage);
            return of(dynamicTexture);
        } catch (IOException e) {
            Common.LOGGER.error("Failed to load texture from file: {}", imageFile.getAbsolutePath(), e);
            return MISSING;
        }
    }

    /**
     * A placeholder texture with id {@code -1} and zero size, used when resolution fails.
     */
    public static ImGuiTexture missing() {
        return MISSING;
    }

    public boolean isMissing() {
        return view == null;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * The opaque id ImGui uses to reference this texture in draw calls.
     * Lazily registers this texture with the active {@link ImGuiRenderer} on first access.
     */
    public long id() {
        if (imguiId == -1) {
            imguiId = register();
        }
        return imguiId;
    }

    private long register() {
        if (view == null) return -1;
        var manager = Client.getImGuiManager();
        if (!(manager instanceof ImGuiManager imguiManager)) return -1;
        return imguiManager.getRenderer().registerTexture(view);
    }

    /**
     * Draws this texture at its native size.
     */
    public void draw() {
        draw(width, height);
    }

    /**
     * Draws this texture scaled to the given size.
     */
    public void draw(float w, float h) {
        if (isMissing()) return;
        var stack = Client.getImGuiManager().getGraphicsStack();
        stack.push();
        stack.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        ImGui.image(id(), w, h, 0, 0, 1, 1);
        stack.pop();
    }

    /**
     * Draws this texture as a clickable image button at the given size.
     *
     * @return true if the button was clicked this frame
     */
    public boolean drawButton(float w, float h) {
        if (isMissing()) return false;
        var stack = Client.getImGuiManager().getGraphicsStack();
        stack.push();
        stack.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        boolean clicked = ImGui.imageButton(id(), w, h, 0, 0, 1, 1);
        stack.pop();
        return clicked;
    }
}