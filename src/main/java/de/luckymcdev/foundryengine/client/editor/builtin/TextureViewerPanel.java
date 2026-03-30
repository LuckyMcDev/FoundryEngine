package de.luckymcdev.foundryengine.client.editor.builtin;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextureViewerPanel extends EditorPanel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Identifier identifier;
    private File file;
    private int textureId = -1;
    private int width;
    private int height;

    public TextureViewerPanel(Identifier id, String title, Identifier identifier) {
        super(id, title);
        this.identifier = identifier;
        loadTexture(Type.IDENTIFIER);
        this.category = PanelCategory.EDITOR_FILES;
    }

    public TextureViewerPanel(Identifier id, String title, File file) {
        super(id, title);
        this.file = file;
        loadTexture(Type.FILE);
        this.category = PanelCategory.EDITOR_FILES;
    }

    private void loadTexture(Type type) {
        if (type == Type.IDENTIFIER) {
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(identifier);
            GpuTextureView textureView = texture.getTextureView();
            if (texture != null) {
                GlTexture glTexture = (GlTexture) texture.getTexture();
                this.textureId = glTexture.glId();
                this.width = textureView.getWidth(textureView.baseMipLevel());
                this.height = textureView.getHeight(textureView.baseMipLevel());
            }
        } else {
            try (InputStream is = new FileInputStream(file)) {
                NativeImage image = NativeImage.read(is);
                DynamicTexture texture = new DynamicTexture(() -> "EditorView_" + file.getName(), image);
                GpuTextureView textureView = texture.getTextureView();
                if (texture != null) {
                    GlTexture glTexture = (GlTexture) texture.getTexture();
                    this.textureId = glTexture.glId();
                    this.width = textureView.getWidth(textureView.baseMipLevel());
                    this.height = textureView.getHeight(textureView.baseMipLevel());
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load texture for viewer: {}", file.getAbsolutePath(), e);
            }
        }

    }

    @Override
    public void content() {
        if (textureId == -1) {
            ImGui.text("Failed to load texture: " + identifier);
            return;
        }

        float availWidth = ImGui.getContentRegionAvailX();
        float displayHeight = (availWidth / width) * height;

        drawImage(textureId, availWidth, displayHeight);

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Resolution: " + width + "x" + height);
            ImGui.text("Path: " + identifier);
            ImGui.endTooltip();
        }
    }

    private void drawImage(int id, float w, float h) {
        GlStateManager._bindTexture(id);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        ImGui.image(id, w, h, 0, 0, 1, 1);
    }

    private enum Type {
        IDENTIFIER(0),
        FILE(1);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}