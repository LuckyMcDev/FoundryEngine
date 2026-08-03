package de.luckymcdev.foundryengine.dsl;

import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * A single run configuration registered in the {@code neoForge.runs} container.
 */
public abstract class RunConfig implements Named {

	private final String name;

	@Inject
	public RunConfig(String name) {
		this.name = name;
		getType().convention("");
		getClient().convention(false);
		getServer().convention(false);
		getClientData().convention(false);
		getServerData().convention(false);
		getProgramArguments().convention(java.util.List.of());
		getSystemProperties().convention(java.util.Map.of());
		getJvmArguments().convention(java.util.List.of());
		getGameDirectory().convention("");
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * NeoForge run type, e.g. {@code gameTestServer}. Leave empty to use the {@code client}/{@code server} helpers.
	 */
	public abstract Property<String> getType();

	public abstract Property<Boolean> getClient();

	public abstract Property<Boolean> getServer();

	public abstract Property<Boolean> getClientData();

	public abstract Property<Boolean> getServerData();

	public abstract ListProperty<String> getProgramArguments();

	public abstract MapProperty<String, String> getSystemProperties();

	public abstract ListProperty<String> getJvmArguments();

	/**
	 * Relative path of the game directory, e.g. {@code runs/client}.
	 */
	public abstract Property<String> getGameDirectory();

	public void client() {
		getClient().set(true);
	}

	public void server() {
		getServer().set(true);
	}

	public void clientData() {
		getClientData().set(true);
	}

	public void serverData() {
		getServerData().set(true);
	}

	public void programArgument(String arg) {
		getProgramArguments().add(arg);
	}

	public void jvmArgument(String arg) {
		getJvmArguments().add(arg);
	}

	public void systemProperty(String key, String value) {
		getSystemProperties().put(key, value);
	}
}