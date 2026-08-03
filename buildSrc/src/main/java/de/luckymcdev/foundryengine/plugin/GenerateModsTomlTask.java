package de.luckymcdev.foundryengine.plugin;

import de.luckymcdev.foundryengine.internal.Toml;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Generates {@code META-INF/neoforge.mods.toml} from the extension model into
 * {@code src/generated/resources}.
 */
public abstract class GenerateModsTomlTask extends DefaultTask {

	@Input
	public abstract Property<String> getModId();

	@Input
	public abstract Property<String> getModVersion();

	@Input
	public abstract Property<String> getModName();

	@Input
	public abstract Property<String> getModLicense();

	@Input
	public abstract Property<String> getModAuthors();

	@Input
	public abstract Property<String> getModDescription();

	@Input
	@Optional
	public abstract Property<String> getDisplayUrl();

	@Input
	@Optional
	public abstract Property<String> getLogoFile();

	@Input
	@Optional
	public abstract Property<String> getCredits();

	@Input
	@Optional
	public abstract Property<String> getIssueTrackerUrl();

	@Input
	@Optional
	public abstract Property<String> getUpdateJsonUrl();

	@Input
	public abstract Property<String> getLoaderVersionRange();

	@Input
	public abstract Property<String> getNeoVersionRange();

	@Input
	public abstract Property<String> getMinecraftVersionRange();

	@Input
	public abstract ListProperty<String> getMixinConfigFiles();

	@Input
	@Optional
	public abstract ListProperty<String> getAccessTransformerFiles();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	@TaskAction
	public void generate() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("modLoader = ").append(Toml.string("javafml")).append('\n');
		sb.append("loaderVersion = ").append(Toml.string(getLoaderVersionRange().get())).append('\n');
		sb.append("license = ").append(Toml.string(getModLicense().get())).append('\n');

		appendOptional(sb, "issueTrackerURL", getIssueTrackerUrl().getOrNull());
		appendOptional(sb, "updateJSONURL", getUpdateJsonUrl().getOrNull());

		sb.append('\n');
		sb.append("[[mods]]\n");
		sb.append("modId = ").append(Toml.string(getModId().get())).append('\n');
		sb.append("version = ").append(Toml.string(getModVersion().get())).append('\n');
		sb.append("displayName = ").append(Toml.string(getModName().get())).append('\n');
		appendOptional(sb, "displayURL", getDisplayUrl().getOrNull());
		appendOptional(sb, "logoFile", getLogoFile().getOrNull());
		appendOptional(sb, "credits", getCredits().getOrNull());
		sb.append("authors = ").append(Toml.string(getModAuthors().get())).append('\n');
		sb.append("description = ").append(Toml.string(getModDescription().get())).append('\n');

		for (String config : getMixinConfigFiles().get()) {
			sb.append('\n');
			sb.append("[[mixins]]\n");
			sb.append("config = ").append(Toml.string(config)).append('\n');
		}

		for (String at : getAccessTransformerFiles().get()) {
			sb.append('\n');
			sb.append("[[accessTransformers]]\n");
			sb.append("file = ").append(Toml.string(at)).append('\n');
		}

		sb.append('\n');
		sb.append("[[dependencies.").append(Toml.string(getModId().get())).append("]]\n");
		sb.append("modId = ").append(Toml.string("neoforge")).append('\n');
		sb.append("type = ").append(Toml.string("required")).append('\n');
		sb.append("versionRange = ").append(Toml.string(getNeoVersionRange().get())).append('\n');
		sb.append("ordering = ").append(Toml.string("NONE")).append('\n');
		sb.append("side = ").append(Toml.string("BOTH")).append('\n');

		sb.append('\n');
		sb.append("[[dependencies.").append(Toml.string(getModId().get())).append("]]\n");
		sb.append("modId = ").append(Toml.string("minecraft")).append('\n');
		sb.append("type = ").append(Toml.string("required")).append('\n');
		sb.append("versionRange = ").append(Toml.string(getMinecraftVersionRange().get())).append('\n');
		sb.append("ordering = ").append(Toml.string("NONE")).append('\n');
		sb.append("side = ").append(Toml.string("BOTH")).append('\n');

		java.nio.file.Path output = getOutputFile().get().getAsFile().toPath();
		Files.createDirectories(output.getParent());
		Files.writeString(output, sb.toString(), StandardCharsets.UTF_8);
		getLogger().lifecycle("Generated {}", output);
	}

	private void appendOptional(StringBuilder sb, String key, String value) {
		if (value != null && !value.isEmpty()) {
			sb.append(key).append(" = ").append(Toml.string(value)).append('\n');
		}
	}
}