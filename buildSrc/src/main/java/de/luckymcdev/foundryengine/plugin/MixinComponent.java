package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.FoundryEngineExtension;
import de.luckymcdev.foundryengine.dsl.MixinConfig;
import de.luckymcdev.foundryengine.dsl.MixinInjectorsOptions;
import de.luckymcdev.foundryengine.dsl.MixinOverwritesOptions;
import de.luckymcdev.foundryengine.dsl.MixinsExtension;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the per-feature mixin JSON configs into
 * {@code src/generated/resources/mixins}.
 */
public class MixinComponent implements FoundryEngineComponent {

	private final FoundryEngineExtension extension;

	public MixinComponent(FoundryEngineExtension extension) {
		this.extension = extension;
	}

	@Override
	public void apply(Project project) {
		MixinsExtension mixins = extension.getMixins();
		String modId = extension.getMod().getId().get();

		var task = project.getTasks().register("generateMixinConfigs", GenerateMixinConfigsTask.class, t -> {
			t.setGroup("foundryengine");
			t.setDescription("Generates the mixin JSON configs from the foundryengine DSL.");
			t.getModId().set(modId);
			t.getBasePackage().set(mixins.getBasePackage());
			t.getOutputDirectory().set(project.getLayout()
				.getProjectDirectory()
				.dir("src/generated/resources"));
		});

		// Populate configs lazily after the DSL is fully configured
		project.afterEvaluate(p -> {
			List<MixinConfigSpec> specs = new ArrayList<>();
			for (MixinConfig config : mixins.getConfigs()) {
				specs.add(toSpec(config));
			}
			task.configure(t -> t.getConfigs().set(specs));
		});

		// Ensure generation runs before resources are processed
		project.getTasks().named("processResources", it -> it.dependsOn(task));
	}

	private MixinConfigSpec toSpec(MixinConfig config) {
		String basePackage = extension.getMixins().getBasePackage().get();
		String pkg = config.resolvePackage(basePackage);
		config.applyDefaults(extension.getMixins());

		MixinInjectorsOptions inj = config.getInjectors();
		MixinOverwritesOptions ovr = config.getOverwrites();

		MixinConfigSpec.InjectorsSpec injectors = new MixinConfigSpec.InjectorsSpec(
			inj.getDefaultRequire().getOrElse(0),
			inj.getDefaultGroup().getOrElse("default"),
			inj.getNamespace().getOrElse(""),
			inj.getInjectionPoints().getOrElse(List.of()),
			inj.getDynamicSelectors().getOrElse(List.of()),
			inj.getMaxShiftBy().getOrElse(0));

		MixinConfigSpec.OverwritesSpec overwrites = new MixinConfigSpec.OverwritesSpec(
			ovr.getConformVisibility().getOrElse(false),
			ovr.getRequireAnnotations().getOrElse(false));

		return new MixinConfigSpec(
			config.getName(),
			pkg,
			config.getRequired().getOrElse(true),
			config.getMinVersion().getOrElse("0.8"),
			config.getCompatibilityLevel().getOrElse("JAVA_25"),
			config.getMixins().getOrElse(List.of()),
			config.getClient().getOrElse(List.of()),
			config.getServer().getOrElse(List.of()),
			config.getRequiredFeatures().getOrElse(List.of()),
			config.getParent().getOrNull(),
			config.getTarget().getOrNull(),
			config.getPriority().getOrElse(1000),
			config.getMixinPriority().getOrElse(1000),
			config.getSetSourceFile().getOrElse(false),
			config.getRefmap().getOrNull(),
			config.getVerbose().getOrElse(false),
			config.getPlugin().getOrNull(),
			injectors,
			overwrites);
	}
}