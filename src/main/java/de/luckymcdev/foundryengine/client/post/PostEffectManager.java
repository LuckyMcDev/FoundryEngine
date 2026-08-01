package de.luckymcdev.foundryengine.client.post;

import de.luckymcdev.foundryengine.client.post.internal.PostEffectEntry;
import de.luckymcdev.foundryengine.client.post.internal.PostEffectRegistry;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public final class PostEffectManager {
	private final PostEffectRegistry registry = new PostEffectRegistry();

	private final PostEffectHandle grayscale;
	private final PostEffectHandle sepia;
	private final PostEffectHandle black;
	private final PostEffectHandle depthVis;
	private final PostEffectHandle star;
	private final PostEffectHandle circle;
	private final PostEffectHandle cinematic;

	private PostEffectEntry currentScreenEffectEntry;
	private float screenEffectTimer;
	private float screenEffectIntro;
	private float screenEffectHold;
	private float screenEffectOutro;
	private LerpType screenEffectLerp = LerpType.LINEAR;
	private boolean inScreenEffect;

	public PostEffectManager() {
		black = cutscene(Common.id("black"));
		star = cutscene(Common.id("star"));
		circle = cutscene(Common.id("circle"));
		cinematic = cutscene(Common.id("cinematic"));

		grayscale = register(Common.id("grayscale"));
		sepia = register(Common.id("sepia"));
		depthVis = register(Common.id("depth_vis"));
	}

	public PostEffectHandle register(Identifier id) {
		PostEffectEntry entry = registry.register(id);
		return new PostEffectHandle(entry, registry);
	}

	public PostEffectHandle register(Identifier id, Consumer<PostEffectConfig> configurator) {
		PostEffectEntry entry = registry.register(id);
		configurator.accept(new PostEffectConfig(entry));
		return new PostEffectHandle(entry, registry);
	}

	public PostEffectHandle register(Identifier id, BooleanSupplier condition) {
		return register(id, cfg -> cfg.when(condition));
	}

	public PostEffectHandle blur(Identifier id, float radius) {
		return register(id, cfg -> cfg.uniform("Radius", radius));
	}

	public PostEffectHandle blur(Identifier id, DoubleSupplier radius) {
		return register(id, cfg -> cfg.uniform("Radius", radius));
	}

	public PostEffectHandle vignette(Identifier id, float intensity) {
		return register(id, cfg -> cfg.uniform("Intensity", intensity));
	}

	public PostEffectHandle vignette(Identifier id, DoubleSupplier intensity) {
		return register(id, cfg -> cfg.uniform("Intensity", intensity));
	}

	public PostEffectHandle cutscene(Identifier id) {
		return register(id, cfg -> cfg
			.phase(RenderPhase.POST_RENDER)
			.priority(999));
	}

	public PostEffectHandle conditionalWithFade(Identifier id, BooleanSupplier condition, int fadeInTicks, int fadeOutTicks) {
		return register(id, cfg -> cfg
			.when(condition)
			.fadeIn(fadeInTicks)
			.fadeOut(fadeOutTicks)
		);
	}

	public PostEffectHandle getGrayscale() {
		return grayscale;
	}

	public PostEffectHandle getSepia() {
		return sepia;
	}

	public PostEffectHandle getBlack() {
		return black;
	}

	public PostEffectHandle getDepthVis() {
		return depthVis;
	}

	public PostEffectHandle getStar() {
		return star;
	}

	public PostEffectHandle getCircle() {
		return circle;
	}

	public PostEffectHandle getCinematic() {
		return cinematic;
	}

	public PostEffectRegistry getRegistry() {
		return registry;
	}

	public boolean inScreenEffect() {
		return inScreenEffect;
	}

	public void startScreenEffect(String name, int intro, int hold, int outro, String lerpType) {
		stopScreenEffect();

		inScreenEffect = true;
		screenEffectTimer = 0;
		screenEffectIntro = intro;
		screenEffectHold = hold;
		screenEffectOutro = outro;
		screenEffectLerp = LerpType.fromString(lerpType);

		PostEffectEntry entry = registry.getEntry(name).orElse(null);
		if (entry == null) {
			return;
		}

		currentScreenEffectEntry = entry;
		entry.setFadeIn(0);
		entry.setFadeOut(0);
		entry.putUniformSlot("Intensity", () -> List.of(new UniformValue.FloatUniform(computeScreenEffectIntensity())));
		entry.setCondition(() -> true);
		entry.setOnAfterApply(ctx -> tickScreenEffect(ctx.deltaTick()));
		entry.setEnabled(true);
	}

	public void stopScreenEffect() {
		if (currentScreenEffectEntry != null) {
			currentScreenEffectEntry.setFadeIn(0);
			currentScreenEffectEntry.setFadeOut(0);
			currentScreenEffectEntry.setCondition(() -> true);
			currentScreenEffectEntry.setOnAfterApply(null);
			currentScreenEffectEntry.setEnabled(false);
			currentScreenEffectEntry = null;
		}
		inScreenEffect = false;
		screenEffectTimer = 0;
	}

	private void tickScreenEffect(float deltaTick) {
		screenEffectTimer += deltaTick;
		float total = screenEffectIntro + screenEffectHold + screenEffectOutro;

		if (screenEffectTimer >= total) {
			stopScreenEffect();
		}
	}

	private float computeScreenEffectIntensity() {
		if (!inScreenEffect || screenEffectTimer <= 0) {
			return 0.0f;
		}
		float totalIntroHold = screenEffectIntro + screenEffectHold;
		if (screenEffectTimer < totalIntroHold) {
			if (screenEffectIntro <= 0) {
				return 1.0f;
			}
			float introProgress = Math.min(1.0f, screenEffectTimer / screenEffectIntro);
			return screenEffectLerp.compute(introProgress);
		}
		float outroProgress = screenEffectOutro <= 0 ? 1.0f : Math.min(1.0f, (screenEffectTimer - totalIntroHold) / screenEffectOutro);
		return 1.0f - screenEffectLerp.compute(outroProgress);
	}
}
