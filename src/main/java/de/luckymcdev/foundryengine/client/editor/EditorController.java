package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.editor.feature.AreaEditorFeature;
import de.luckymcdev.foundryengine.client.editor.feature.CutsceneEditorFeature;
import de.luckymcdev.foundryengine.client.editor.feature.EditorFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class EditorController {
    public static final Item EDITOR_ITEM = Items.DEBUG_STICK;
    private final List<EditorFeature> features = new ArrayList<>();
    private final CutsceneEditorFeature cutsceneEditorFeature = new CutsceneEditorFeature();
    private final AreaEditorFeature areaEditorFeature = new AreaEditorFeature();

    public EditorController() {
        features.add(cutsceneEditorFeature);
        features.add(areaEditorFeature);
    }

    public static boolean isEditorStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == EDITOR_ITEM;
    }

    public static boolean isHoldingEditorItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return isEditorStack(mc.player.getMainHandItem()) || isEditorStack(mc.player.getOffhandItem());
    }

    public static boolean isUsingEditorItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || mc.isPaused()) return false;
        return isHoldingEditorItem() && mc.options.keyUse.isDown();
    }

    public CutsceneEditorFeature getCutsceneEditorFeature() {
        return cutsceneEditorFeature;
    }

    public AreaEditorFeature getAreaEditorFeature() {
        return areaEditorFeature;
    }


    public void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!isHoldingEditorItem()) {
            features.forEach(EditorFeature::onDeactivated);
            return;
        }

        features.forEach(EditorFeature::clientTick);
    }

    public boolean onScroll(double vertical) {
        for (EditorFeature f : features) {
            if (f.onScroll(vertical)) return true;
        }
        return false;
    }

    public void renderFeatures() {
        if (!isHoldingEditorItem()) return;
        features.forEach(EditorFeature::render);
    }
}
