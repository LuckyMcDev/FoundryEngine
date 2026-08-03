package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.List;

/**
 * Shared option surface for a mixin config ({@link MixinConfig}) and for the
 * global {@code mixins} block ({@link MixinsExtension}). Options written
 * directly in the {@code mixins} block act as defaults for every config; an
 * option explicitly set on a config wins over them.
 */
public abstract class MixinOptions {

	private final MixinInjectorsOptions injectors;
	private final MixinOverwritesOptions overwrites;

	@Inject
	public MixinOptions(ObjectFactory objects) {
		this.injectors = objects.newInstance(MixinInjectorsOptions.class);
		this.overwrites = objects.newInstance(MixinOverwritesOptions.class);
	}

	private static void adoptList(ListProperty<String> target, ListProperty<String> defaults) {
		if (!target.isPresent()) {
			List<String> values = defaults.getOrNull();
			if (values != null && !values.isEmpty()) {
				target.set(values);
			}
		}
	}

	public abstract Property<Boolean> getRequired();

	public abstract Property<String> getMinVersion();

	public abstract Property<String> getCompatibilityLevel();

	public abstract ListProperty<String> getMixins();

	public abstract ListProperty<String> getClient();

	/**
	 * Mixin classes to load only on a dedicated server.
	 */
	public abstract ListProperty<String> getServer();

	/**
	 * Required feature flags, used instead of {@code minVersion} for sanity checking.
	 */
	public abstract ListProperty<String> getRequiredFeatures();

	/**
	 * Name of the parent configuration to inherit options from.
	 */
	public abstract Property<String> getParent();

	/**
	 * Target selector, e.g. {@code @env(DEFAULT)}.
	 */
	public abstract Property<String> getTarget();

	/**
	 * Configuration priority (default 1000).
	 */
	public abstract Property<Integer> getPriority();

	/**
	 * Default priority for mixins in this config (default 1000).
	 */
	public abstract Property<Integer> getMixinPriority();

	/**
	 * Whether to set the {@code sourceFile} property when applying mixins.
	 */
	public abstract Property<Boolean> getSetSourceFile();

	/**
	 * The path to the reference map resource.
	 */
	public abstract Property<String> getRefmap();

	/**
	 * Whether to log extra information.
	 */
	public abstract Property<Boolean> getVerbose();

	/**
	 * Fully-qualified name of the {@code IMixinConfigPlugin} for this config.
	 */
	public abstract Property<String> getPlugin();

	public MixinInjectorsOptions getInjectors() {
		return injectors;
	}

	public MixinOverwritesOptions getOverwrites() {
		return overwrites;
	}

	public void mixin(String name) {
		getMixins().add(name);
	}

	public void clientMixin(String name) {
		getClient().add(name);
	}

	public void serverMixin(String name) {
		getServer().add(name);
	}

	public void requiredFeature(String feature) {
		getRequiredFeatures().add(feature);
	}

	public void plugin(String name) {
		getPlugin().set(name);
	}

	public void injectors(Action<? super MixinInjectorsOptions> action) {
		action.execute(injectors);
	}

	public void overwrites(Action<? super MixinOverwritesOptions> action) {
		action.execute(overwrites);
	}

	/**
	 * Adopts options set on {@code defaults} (the global {@code mixins} block)
	 * that this config has not explicitly set itself.
	 */
	public void applyDefaults(MixinOptions defaults) {
		if (!getRequired().isPresent()) {
			getRequired().convention(defaults.getRequired());
		}
		if (!getMinVersion().isPresent()) {
			getMinVersion().convention(defaults.getMinVersion());
		}
		if (!getCompatibilityLevel().isPresent()) {
			getCompatibilityLevel().convention(defaults.getCompatibilityLevel());
		}
		if (!getParent().isPresent()) {
			getParent().convention(defaults.getParent());
		}
		if (!getTarget().isPresent()) {
			getTarget().convention(defaults.getTarget());
		}
		if (!getPriority().isPresent()) {
			getPriority().convention(defaults.getPriority());
		}
		if (!getMixinPriority().isPresent()) {
			getMixinPriority().convention(defaults.getMixinPriority());
		}
		if (!getSetSourceFile().isPresent()) {
			getSetSourceFile().convention(defaults.getSetSourceFile());
		}
		if (!getRefmap().isPresent()) {
			getRefmap().convention(defaults.getRefmap());
		}
		if (!getVerbose().isPresent()) {
			getVerbose().convention(defaults.getVerbose());
		}
		if (!getPlugin().isPresent()) {
			getPlugin().convention(defaults.getPlugin());
		}
		adoptList(getMixins(), defaults.getMixins());
		adoptList(getClient(), defaults.getClient());
		adoptList(getServer(), defaults.getServer());
		adoptList(getRequiredFeatures(), defaults.getRequiredFeatures());
		injectors.applyDefaultsIfAbsent(defaults.injectors);
		overwrites.applyDefaultsIfAbsent(defaults.overwrites);
	}
}