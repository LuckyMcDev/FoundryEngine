package de.luckymcdev.foundryengine.mixin.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.post.PrioritizedEffect;
import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import de.luckymcdev.foundryengine.interfaces.EnginePostChain;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * Mixin to render ImGui and custom Post Chain stuff
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin implements EngineGameRenderer {
    @Unique
    private final SortedSet<PrioritizedEffect> engine$activeEffects = new TreeSet<>();
    @Shadow
    @Final
    public CrossFrameResourcePool resourcePool;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Override
    @Inject(method = "render", at = @At("HEAD"))
    public void engine$renderHead(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;")
    )
    private @Nullable PostChain engine$disableVanillaEffectLookup(ShaderManager instance, Identifier id, Set<Identifier> allowedTargets) {
        return null;
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0)
    )
    private void engine$injectMultiPostProcess(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        if (!engine$activeEffects.isEmpty()) {
            ShaderManager shaderManager = this.minecraft.getShaderManager();
            Map<Identifier, RenderTarget> availableTargets = new HashMap<>();

            availableTargets.put(LevelTargetBundle.MAIN_TARGET_ID, this.minecraft.getMainRenderTarget());
            var levelRenderer = this.minecraft.levelRenderer;

            if (levelRenderer.entityOutlineTarget() != null)
                availableTargets.put(LevelTargetBundle.ENTITY_OUTLINE_TARGET_ID, levelRenderer.entityOutlineTarget());
            if (levelRenderer.getTranslucentTarget() != null)
                availableTargets.put(LevelTargetBundle.TRANSLUCENT_TARGET_ID, levelRenderer.getTranslucentTarget());
            if (levelRenderer.getItemEntityTarget() != null)
                availableTargets.put(LevelTargetBundle.ITEM_ENTITY_TARGET_ID, levelRenderer.getItemEntityTarget());
            if (levelRenderer.getWeatherTarget() != null)
                availableTargets.put(LevelTargetBundle.WEATHER_TARGET_ID, levelRenderer.getWeatherTarget());
            if (levelRenderer.getCloudsTarget() != null)
                availableTargets.put(LevelTargetBundle.CLOUDS_TARGET_ID, levelRenderer.getCloudsTarget());
            if (levelRenderer.getParticlesTarget() != null)
                availableTargets.put(LevelTargetBundle.PARTICLES_TARGET_ID, levelRenderer.getParticlesTarget());

            Set<Identifier> allowedTargets = availableTargets.keySet();

            for (PrioritizedEffect effect : engine$activeEffects) {
                try {
                    PostChain chain = shaderManager.getPostChain(effect.id(), allowedTargets);
                    if (chain != null) {
                        ((EnginePostChain) chain).engine$process(availableTargets, this.resourcePool);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public Collection<Identifier> engine$getActiveEffects() {
        return engine$activeEffects.stream()
                .map(PrioritizedEffect::id)
                .toList();
    }

    @Override
    public void engine$clearEffects() {
        engine$activeEffects.clear();
    }

    @Override
    public void engine$addEffect(Identifier id, int priority) {
        engine$activeEffects.add(new PrioritizedEffect(id, priority));
    }

    @Override
    public void engine$removeEffect(Identifier id) {
        engine$activeEffects.removeIf(e -> e.id().equals(id));
    }

    @Override
    public void engine$invalidate(Identifier id) {
        Optional<PostChain> old = minecraft.getShaderManager().compilationCache.postChains.remove(id);
        old.ifPresent(PostChain::close);
    }

    @Inject(method = "setPostEffect", at = @At("HEAD"), cancellable = true)
    private void engine$interceptVanillaSetEffect(Identifier id, CallbackInfo ci) {
        engine$addEffect(id, 0);
        ci.cancel();
    }

    @Inject(method = "clearPostEffect", at = @At("HEAD"), cancellable = true)
    private void engine$interceptVanillaClearEffect(CallbackInfo ci) {
        engine$activeEffects.removeIf(e -> e.priority() == 0);
        ci.cancel();
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void engine$renderReturn(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        var mainMenu = Client.getMainMenu();
        var imguiManager = Client.getImGuiManager();
        var editorManager = Client.getEditorManager();

        if (imguiManager.isEnabled()) {
            try {
                imguiManager.begin();
                mainMenu.handleRender();
                editorManager.handleRender();
            } finally {
                imguiManager.end();
            }
        }
    }
}