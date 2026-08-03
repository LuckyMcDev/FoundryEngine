package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.dsl.MixinConfig;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the per-feature mixin JSON configs into
 * {@code src/generated/resources/mixins}.
 */
public abstract class GenerateMixinConfigsTask extends DefaultTask {

	private static String field(String key, String value) {
		return "\"" + key + "\": " + value;
	}

	private static String quoted(String value) {
		return "\"" + value + "\"";
	}

	private static void addIfPresent(List<String> fields, String key, String value) {
		if (value != null && !value.isEmpty()) {
			fields.add(field(key, quoted(value)));
		}
	}

	private static void addArray(List<String> fields, String key, List<String> values) {
		fields.add(arrayField(field(key, ""), values, 0));
	}

	private static void addArrayIfNotEmpty(List<String> fields, String key, List<String> values) {
		if (!values.isEmpty()) {
			fields.add(arrayField(field(key, ""), values, 0));
		}
	}

	/**
	 * Renders {@code "key": [ "a", "b" ]} as a single field string containing newlines.
	 */
	private static String arrayField(String header, List<String> values, int indent) {
		StringBuilder sb = new StringBuilder(header);
		if (values.isEmpty()) {
			sb.append('[').append(']');
			return sb.toString();
		}
		String pad = "\t".repeat(indent);
		String padInner = "\t".repeat(indent + 1);
		sb.append("[\n");
		for (String value : values) {
			sb.append(padInner).append('"').append(value).append("\",\n");
		}
		sb.setLength(sb.length() - 2);
		sb.append('\n').append(pad).append(']');
		return sb.toString();
	}

	private static void addNestedArray(List<String> fields, String key, List<String> values) {
		if (!values.isEmpty()) {
			fields.add(arrayField(field(key, ""), values, 3));
		}
	}

	/**
	 * Renders {@code "key": { inner... }} as a single field string honoring newlines.
	 */
	private static String objectField(String key, List<String> inner) {
		StringBuilder sb = new StringBuilder(field(key, ""));
		sb.append("{\n");
		for (int i = 0; i < inner.size(); i++) {
			sb.append('\t').append('\t').append(inner.get(i));
			if (i < inner.size() - 1) {
				sb.append(',');
			}
			sb.append('\n');
		}
		sb.append('\t').append('}');
		return sb.toString();
	}

	@Input
	public abstract Property<String> getModId();

	@Input
	public abstract Property<String> getBasePackage();

	@Nested
	public abstract ListProperty<MixinConfigSpec> getConfigs();

	@OutputDirectory
	public abstract org.gradle.api.file.DirectoryProperty getOutputDirectory();

	@TaskAction
	public void generate() throws IOException {
		String modId = getModId().get();
		String basePackage = getBasePackage().get();
		Path outputDir = getOutputDirectory().get().getAsFile().toPath();
		Files.createDirectories(outputDir);

		for (MixinConfigSpec spec : getConfigs().get()) {
			String fileName = MixinConfig.fileName(modId, spec.getName());
			Path file = outputDir.resolve(fileName).normalize();
			if (!file.startsWith(outputDir)) {
				throw new IllegalArgumentException("Mixin file name escapes output directory: " + fileName);
			}
			Files.createDirectories(file.getParent());
			Files.writeString(file, render(spec, basePackage), StandardCharsets.UTF_8);
			getLogger().lifecycle("Generated {}", file);
		}
	}

	private String render(MixinConfigSpec spec, String basePackage) {
		String pkg = spec.getPackageName();
		if (pkg == null || pkg.isEmpty()) {
			pkg = spec.getName().isEmpty() ? basePackage : basePackage + "." + spec.getName();
		}

		List<String> fields = new ArrayList<>();
		fields.add(field("required", String.valueOf(spec.isRequired())));
		fields.add(field("minVersion", quoted(spec.getMinVersion())));
		fields.add(field("package", quoted(pkg)));
		fields.add(field("compatibilityLevel", quoted(spec.getCompatibilityLevel())));
		addIfPresent(fields, "parent", spec.getParent());
		addIfPresent(fields, "target", spec.getTarget());
		if (spec.getPriority() != 1000) {
			fields.add(field("priority", String.valueOf(spec.getPriority())));
		}
		if (spec.getMixinPriority() != 1000) {
			fields.add(field("mixinPriority", String.valueOf(spec.getMixinPriority())));
		}
		addArray(fields, "mixins", spec.getMixins());
		addArray(fields, "client", spec.getClient());
		addArrayIfNotEmpty(fields, "server", spec.getServer());
		addArrayIfNotEmpty(fields, "requiredFeatures", spec.getRequiredFeatures());
		if (spec.isSetSourceFile()) {
			fields.add(field("setSourceFile", "true"));
		}
		addIfPresent(fields, "refmap", spec.getRefmap());
		if (spec.isVerbose()) {
			fields.add(field("verbose", "true"));
		}
		addIfPresent(fields, "plugin", spec.getPlugin());
		addInjectors(fields, spec.getInjectors());
		addOverwrites(fields, spec.getOverwrites());

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for (int i = 0; i < fields.size(); i++) {
			sb.append('\t').append(fields.get(i));
			if (i < fields.size() - 1) {
				sb.append(',');
			}
			sb.append('\n');
		}
		sb.append("}\n");
		return sb.toString();
	}

	private void addInjectors(List<String> fields, MixinConfigSpec.InjectorsSpec inj) {
		List<String> inner = new ArrayList<>();
		if (inj.getDefaultRequire() != 0) {
			inner.add(field("defaultRequire", String.valueOf(inj.getDefaultRequire())));
		}
		if (!"default".equals(inj.getDefaultGroup())) {
			inner.add(field("defaultGroup", quoted(inj.getDefaultGroup())));
		}
		if (!inj.getNamespace().isEmpty()) {
			inner.add(field("namespace", quoted(inj.getNamespace())));
		}
		addNestedArray(inner, "injectionPoints", inj.getInjectionPoints());
		addNestedArray(inner, "dynamicSelectors", inj.getDynamicSelectors());
		if (inj.getMaxShiftBy() != 0) {
			inner.add(field("maxShiftBy", String.valueOf(inj.getMaxShiftBy())));
		}
		if (!inner.isEmpty()) {
			fields.add(objectField("injectors", inner));
		}
	}

	private void addOverwrites(List<String> fields, MixinConfigSpec.OverwritesSpec ovr) {
		List<String> inner = new ArrayList<>();
		if (ovr.isConformVisibility()) {
			inner.add(field("conformVisibility", "true"));
		}
		if (ovr.isRequireAnnotations()) {
			inner.add(field("requireAnnotations", "true"));
		}
		if (!inner.isEmpty()) {
			fields.add(objectField("overwrites", inner));
		}
	}
}
