package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Mixin configuration. Declares per-feature configs and the base mixin package.
 * Any option set directly on this block (outside a {@code config}) acts as a
 * default for every config.
 */
public abstract class MixinsExtension extends MixinOptions {

	private static final String DEFAULT_PLUGIN = "de.luckymcdev.foundryengine.mixin.FoundryEngineMixinPlugin";

	@Inject
	public MixinsExtension(ObjectFactory objects) {
		super(objects);
		getBasePackage().convention("de.luckymcdev.foundryengine.mixin");
		getRequired().convention(true);
		getMinVersion().convention("0.8");
		getCompatibilityLevel().convention("JAVA_25");
		getPriority().convention(1000);
		getMixinPriority().convention(1000);
		getSetSourceFile().convention(false);
		getVerbose().convention(false);
		getPlugin().convention(DEFAULT_PLUGIN);
		getInjectors().getDefaultRequire().convention(1);
		getInjectors().getDefaultGroup().convention("default");
		getInjectors().getNamespace().convention("");
		getInjectors().getMaxShiftBy().convention(0);
		getOverwrites().getConformVisibility().convention(false);
		getOverwrites().getRequireAnnotations().convention(true);
	}

	/**
	 * Per-feature mixin configs.
	 */
	public abstract NamedDomainObjectContainer<MixinConfig> getConfigs();

	/**
	 * Base package used for configs that do not declare their own package.
	 */
	public abstract Property<String> getBasePackage();

	/**
	 * Registers a mixin config, e.g. {@code mixins.render { clientMixin 'Foo' }}.
	 * Global options from this block are adopted as defaults for the config
	 * before the {@code action} runs, so explicit config options win.
	 */
	public void config(String name, Action<? super MixinConfig> action) {
		MixinConfig config = getConfigs().maybeCreate(name);
		config.applyDefaults(this);
		action.execute(config);
	}
}