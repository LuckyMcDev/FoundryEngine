package de.luckymcdev.foundryengine.client.imgui;

import foundry.imgui.api.ImGuiMC;
import foundry.imgui.api.ImGuiTextureProvider;
import imgui.ImVec2;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jspecify.annotations.Nullable;

public class ImTexture {
	public static final ImTexture EMPTY = new ImTexture(null, false, -1, -1);
	private final ImVec2 size;
	private @Nullable AbstractTexture texture;
	private boolean owningTexture;

	public ImTexture(@Nullable AbstractTexture texture, boolean owningTexture, int width, int height) {
		this.texture = texture;
		this.owningTexture = owningTexture;
		this.size = new ImVec2(width, height);
	}

	public void setTexture(AbstractTexture texture, boolean owningTexture) {
		this.texture = texture;
		this.owningTexture = owningTexture;
	}

	public @Nullable AbstractTexture getTexture() {
		return texture;
	}

	public @Nullable ImGuiTextureProvider getProvider() {
		return ImGuiMC.getTexture(this.texture);
	}

	public ImVec2 getSize() {
		return size;
	}

	public void setSize(ImVec2 size) {
		this.size.set(size);
	}

	public float width() {
		return (int) size.x;
	}

	public float height() {
		return (int) size.y;
	}

	public void close() {
		if (this.owningTexture) {
			this.texture.close();
		} else {
			this.texture = null;
			this.size.set(0, 0);
		}
	}
}
