package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TextFieldWidget extends TextWidget {
	public TextFieldWidget(UIVec position, UIVec size) {
		super(position, size);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
		if (childClicked) {
			return true;
		}
		this.setFocused(true);
		return true;
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (this.isFocused()) {
			if (key == GLFW.GLFW_KEY_BACKSPACE) {
				String stringText = this.getText().getString();
				this.setText(Component.literal(stringText.subSequence(0, Math.max(0, stringText.length() - 1)).toString()).setStyle(this.getText().getStyle()));
			}
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char c, int modifiers) {
		if (this.isFocused()) {
			this.setText(this.getText().append(String.valueOf(c)));
		}
		return super.charTyped(c, modifiers);
	}
}