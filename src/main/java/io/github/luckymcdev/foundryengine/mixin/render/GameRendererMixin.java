package io.github.luckymcdev.foundryengine.mixin.render;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.EditorManager;
import io.github.luckymcdev.foundryengine.client.editor.MainMenu;
import io.github.luckymcdev.foundryengine.interfaces.TbGameRenderer;
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
public class GameRendererMixin implements TbGameRenderer {

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
            MainMenu.handleRender();
            tb$EDITOR_MANAGER.handleRender();
        }
        Client.getImGuiManager().end();
    }
}