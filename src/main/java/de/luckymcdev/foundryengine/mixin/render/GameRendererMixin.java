package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.feature.EngineFeatures;
import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render ImGui.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin implements EngineGameRenderer {
    @Override
    @Inject(method = "render", at = @At("HEAD"))
    public void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        var mainMenu = Client.getMainMenu();
        var imguiManager = Client.getImGuiManager();
        var editorManager = Client.getEditorManager();
        var featureManager = Common.getFeatureManager();

        if (featureManager.isEnabled(EngineFeatures.EDITOR) && imguiManager.isEnabled()) {
            imguiManager.begin();
            mainMenu.handleShortcuts();
            mainMenu.render();
            editorManager.handleRender();
            imguiManager.end();
        }
    }
}