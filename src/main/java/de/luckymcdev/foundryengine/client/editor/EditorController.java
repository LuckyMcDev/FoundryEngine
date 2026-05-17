package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.cutscene.CutsceneEditor;
import de.luckymcdev.foundryengine.client.editor.feature.AreaFeature;
import de.luckymcdev.foundryengine.client.editor.feature.EditorFeature;
import de.luckymcdev.foundryengine.common.item.ModItems;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class EditorController {
    private final List<EditorFeature> features = new ArrayList<>();
    private final CutsceneEditor cutsceneEditor = new CutsceneEditor();

    public EditorController() {
        features.add(cutsceneEditor);
        features.add(new AreaFeature());
    }

    public static boolean isHoldingEditorItem() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ModItems.EDITOR_ITEM == null) return false;
        return mc.player.getMainHandItem().getItem() == ModItems.EDITOR_ITEM
                || mc.player.getOffhandItem().getItem() == ModItems.EDITOR_ITEM;
    }

    public CutsceneEditor getCutsceneEditor() {
        return cutsceneEditor;
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
