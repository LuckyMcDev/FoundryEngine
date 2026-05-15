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
import java.util.function.Supplier;

/**
 * Mixin to render ImGui and custom Post Chain stuff
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin implements EngineGameRenderer {
    @Unique
    private final SortedSet<PrioritizedEffect> engine$activeEffects = new TreeSet<>();
    @Unique
    private final Map<Identifier, RenderTarget> engine$frameTargets = HashMap.newHashMap(10);
    @Unique
    private final List<Map.Entry<Identifier, Supplier<@Nullable RenderTarget>>> engine$targetResolvers = new ArrayList<>();
    @Shadow
    @Final
    public CrossFrameResourcePool resourcePool;
    @Shadow
    @Final
    private Minecraft minecraft;

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
        if (engine$activeEffects.isEmpty()) return;

        if (engine$targetResolvers.isEmpty()) {
            var lr = this.minecraft.levelRenderer;
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.MAIN_TARGET_ID, this.minecraft::getMainRenderTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.ENTITY_OUTLINE_TARGET_ID, lr::entityOutlineTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.TRANSLUCENT_TARGET_ID, lr::getTranslucentTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.ITEM_ENTITY_TARGET_ID, lr::getItemEntityTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.WEATHER_TARGET_ID, lr::getWeatherTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.CLOUDS_TARGET_ID, lr::getCloudsTarget));
            engine$targetResolvers.add(Map.entry(LevelTargetBundle.PARTICLES_TARGET_ID, lr::getParticlesTarget));
        }

        engine$frameTargets.clear();

        for (Map.Entry<Identifier, Supplier<RenderTarget>> entry : engine$targetResolvers) {
            RenderTarget target = entry.getValue().get();
            if (target != null) {
                engine$frameTargets.put(entry.getKey(), target);
            }
        }

        Set<Identifier> allowedTargets = engine$frameTargets.keySet();
        ShaderManager shaderManager = this.minecraft.getShaderManager();

        for (PrioritizedEffect effect : engine$activeEffects) {
            PostChain chain = shaderManager.getPostChain(effect.id(), allowedTargets);
            if (chain instanceof EnginePostChain engineChain) {
                try {
                    engineChain.engine$process(engine$frameTargets, this.resourcePool);
                } catch (Exception e) {
                    Client.LOGGER.error("Foundry Engine: Failed to process post-effect [{}]", effect.id(), e);
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
    @Inject(method = "render", at = @At("HEAD"))
    public void engine$renderHead(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
    }

    @Override
    @Inject(method = "render", at = @At("RETURN"))
    public void engine$renderReturn(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        var imguiManager = Client.getImGuiManager();

        if (imguiManager.isEnabled()) {
            var mainMenu = Client.getMainMenu();
            var editorManager = Client.getEditorManager();

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