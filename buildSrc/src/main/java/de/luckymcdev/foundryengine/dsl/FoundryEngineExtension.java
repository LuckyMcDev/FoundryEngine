package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

/**
 * Root {@code foundryengine} extension. Groups per-concern configuration into
 * nested sub-blocks.
 */
public class FoundryEngineExtension {

	private final ModExtension mod;
	private final MinecraftExtension minecraft;
	private final MixinsExtension mixins;
	private final BundlesExtension bundles;
	private final PublishingExtension publishing;
	private final VitePressExtension vitepress;

	@Inject
	public FoundryEngineExtension(ObjectFactory objects) {
		mod = objects.newInstance(ModExtension.class);
		minecraft = objects.newInstance(MinecraftExtension.class);
		mixins = objects.newInstance(MixinsExtension.class);
		bundles = objects.newInstance(BundlesExtension.class);
		publishing = objects.newInstance(PublishingExtension.class);
		vitepress = objects.newInstance(VitePressExtension.class);
	}

	public ModExtension getMod() {
		return mod;
	}

	public void mod(Action<? super ModExtension> action) {
		action.execute(mod);
	}

	public MinecraftExtension getMinecraft() {
		return minecraft;
	}

	public void minecraft(Action<? super MinecraftExtension> action) {
		action.execute(minecraft);
	}

	public MixinsExtension getMixins() {
		return mixins;
	}

	public void mixins(Action<? super MixinsExtension> action) {
		action.execute(mixins);
	}

	public BundlesExtension getBundles() {
		return bundles;
	}

	public void bundles(Action<? super BundlesExtension> action) {
		action.execute(bundles);
	}

	public PublishingExtension getPublishing() {
		return publishing;
	}

	public void publishing(Action<? super PublishingExtension> action) {
		action.execute(publishing);
	}

	public VitePressExtension getVitepress() {
		return vitepress;
	}

	public void vitepress(Action<? super VitePressExtension> action) {
		action.execute(vitepress);
	}
}