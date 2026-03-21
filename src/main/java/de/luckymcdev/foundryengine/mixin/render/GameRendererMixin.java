package de.luckymcdev.foundryengine.mixin.render;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render ImGui.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin implements EngineGameRenderer {

    @Unique
    private static final EditorManager tb$EDITOR_MANAGER = Client.getEditorManager();

    @Override
    @Inject(method = "render", at = @At("HEAD"))
    public void tb$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void tb$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Client.getImGuiManager().begin();
        if (Client.getImGuiManager().isEnabled()) {
            Client.getMainMenu().handleShortcuts();
            Client.getMainMenu().render();
            tb$EDITOR_MANAGER.handleRender();
        }
        Client.getImGuiManager().end();
    }
}