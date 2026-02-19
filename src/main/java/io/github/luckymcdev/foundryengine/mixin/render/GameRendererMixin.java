package io.github.luckymcdev.foundryengine.mixin.render;

import io.github.luckymcdev.foundryengine.client.editor.BuiltInEditor;
import io.github.luckymcdev.foundryengine.client.editor.MainMenu;
import io.github.luckymcdev.foundryengine.client.imgui.ImGuiManager;

import io.github.luckymcdev.foundryengine.common.Instances;
import io.github.luckymcdev.foundryengine.interfaces.TbGameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements TbGameRenderer {

    @Unique
    private static final BuiltInEditor tb$builtInEditor = Instances.getBuiltInEditor();

    @Override
    @Inject(method = "render", at = @At("HEAD"))
    public void tb$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void tb$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Instances.getImGuiManager().begin();

        // Render the main menu bar at the top
        MainMenu.handleRender();

        // Render all open panels
        tb$builtInEditor.handleRender();

        Instances.getImGuiManager().end();
    }
}