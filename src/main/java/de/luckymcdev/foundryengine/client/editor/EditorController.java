package de.luckymcdev.foundryengine.client.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EditorController {
	public static final Item EDITOR_ITEM = Items.DEBUG_STICK;
	private final CutsceneTool cutsceneTool = new CutsceneTool();
	private final AreaTool areaTool = new AreaTool();

	public static boolean isEditorStack(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == EDITOR_ITEM;
	}

	public static boolean isHoldingEditorItem() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return false;
		}
		return isEditorStack(mc.player.getMainHandItem()) || isEditorStack(mc.player.getOffhandItem());
	}

	public static boolean isUsingEditorItem() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null || mc.player == null || mc.isPaused()) {
			return false;
		}
		return isHoldingEditorItem() && mc.options.keyUse.isDown();
	}

	public CutsceneTool getCutsceneTool() {
		return cutsceneTool;
	}

	public AreaTool getAreaTool() {
		return areaTool;
	}

	public void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			return;
		}

		if (!isHoldingEditorItem()) {
			cutsceneTool.onDeactivated();
			areaTool.onDeactivated();
			return;
		}

		cutsceneTool.tick();
		areaTool.tick();
	}

	public boolean onScroll(double vertical) {
		if (!isHoldingEditorItem()) {
			return false;
		}
		if (cutsceneTool.onScroll(vertical)) {
			return true;
		}
		return areaTool.onScroll(vertical);
	}

	public void renderFeatures() {
		if (!isHoldingEditorItem()) {
			return;
		}
		cutsceneTool.render();
		areaTool.render();
	}
}
