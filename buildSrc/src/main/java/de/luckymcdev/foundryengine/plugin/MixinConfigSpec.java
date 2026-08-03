package de.luckymcdev.foundryengine.plugin;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable, serialisable spec for a single generated mixin config, captured
 * from the DSL so the task does not hold references to the extension
 * (config-cache safe).
 */
public class MixinConfigSpec implements Serializable {

	private static final long serialVersionUID = 2L;

	private final String name;
	private final String packageName;
	private final boolean required;
	private final String minVersion;
	private final String compatibilityLevel;
	private final List<String> mixins;
	private final List<String> client;
	private final List<String> server;
	private final List<String> requiredFeatures;
	private final String parent;
	private final String target;
	private final int priority;
	private final int mixinPriority;
	private final boolean setSourceFile;
	private final String refmap;
	private final boolean verbose;
	private final String plugin;
	private final InjectorsSpec injectors;
	private final OverwritesSpec overwrites;

	public MixinConfigSpec(String name, String packageName, boolean required, String minVersion,
	                       String compatibilityLevel, List<String> mixins, List<String> client, List<String> server,
	                       List<String> requiredFeatures, String parent, String target, int priority, int mixinPriority,
	                       boolean setSourceFile, String refmap, boolean verbose, String plugin,
	                       InjectorsSpec injectors, OverwritesSpec overwrites) {
		this.name = name;
		this.packageName = packageName;
		this.required = required;
		this.minVersion = minVersion;
		this.compatibilityLevel = compatibilityLevel;
		this.mixins = new ArrayList<>(mixins);
		this.client = new ArrayList<>(client);
		this.server = new ArrayList<>(server);
		this.requiredFeatures = new ArrayList<>(requiredFeatures);
		this.parent = parent;
		this.target = target;
		this.priority = priority;
		this.mixinPriority = mixinPriority;
		this.setSourceFile = setSourceFile;
		this.refmap = refmap;
		this.verbose = verbose;
		this.plugin = plugin;
		this.injectors = injectors;
		this.overwrites = overwrites;
	}

	@Input
	public String getName() {
		return name;
	}

	@Input
	public String getPackageName() {
		return packageName;
	}

	@Input
	public boolean isRequired() {
		return required;
	}

	@Input
	public String getMinVersion() {
		return minVersion;
	}

	@Input
	public String getCompatibilityLevel() {
		return compatibilityLevel;
	}

	@Input
	public List<String> getMixins() {
		return mixins;
	}

	@Input
	public List<String> getClient() {
		return client;
	}

	@Input
	public List<String> getServer() {
		return server;
	}

	@Input
	public List<String> getRequiredFeatures() {
		return requiredFeatures;
	}

	@Input
	@Optional
	public String getParent() {
		return parent;
	}

	@Input
	@Optional
	public String getTarget() {
		return target;
	}

	@Input
	public int getPriority() {
		return priority;
	}

	@Input
	public int getMixinPriority() {
		return mixinPriority;
	}

	@Input
	public boolean isSetSourceFile() {
		return setSourceFile;
	}

	@Input
	@Optional
	public String getRefmap() {
		return refmap;
	}

	@Input
	public boolean isVerbose() {
		return verbose;
	}

	@Input
	@Optional
	public String getPlugin() {
		return plugin;
	}

	@Nested
	public InjectorsSpec getInjectors() {
		return injectors;
	}

	@Nested
	public OverwritesSpec getOverwrites() {
		return overwrites;
	}

	/**
	 * Serialisable injectors options.
	 */
	public static class InjectorsSpec implements Serializable {

		private static final long serialVersionUID = 1L;

		private final int defaultRequire;
		private final String defaultGroup;
		private final String namespace;
		private final List<String> injectionPoints;
		private final List<String> dynamicSelectors;
		private final int maxShiftBy;

		public InjectorsSpec(int defaultRequire, String defaultGroup, String namespace,
		                     List<String> injectionPoints, List<String> dynamicSelectors, int maxShiftBy) {
			this.defaultRequire = defaultRequire;
			this.defaultGroup = defaultGroup;
			this.namespace = namespace;
			this.injectionPoints = new ArrayList<>(injectionPoints);
			this.dynamicSelectors = new ArrayList<>(dynamicSelectors);
			this.maxShiftBy = maxShiftBy;
		}

		@Input
		public int getDefaultRequire() {
			return defaultRequire;
		}

		@Input
		@Optional
		public String getDefaultGroup() {
			return defaultGroup;
		}

		@Input
		@Optional
		public String getNamespace() {
			return namespace;
		}

		@Input
		public List<String> getInjectionPoints() {
			return injectionPoints;
		}

		@Input
		public List<String> getDynamicSelectors() {
			return dynamicSelectors;
		}

		@Input
		public int getMaxShiftBy() {
			return maxShiftBy;
		}
	}

	/**
	 * Serialisable overwrites options.
	 */
	public static class OverwritesSpec implements Serializable {

		private static final long serialVersionUID = 1L;

		private final boolean conformVisibility;
		private final boolean requireAnnotations;

		public OverwritesSpec(boolean conformVisibility, boolean requireAnnotations) {
			this.conformVisibility = conformVisibility;
			this.requireAnnotations = requireAnnotations;
		}

		@Input
		public boolean isConformVisibility() {
			return conformVisibility;
		}

		@Input
		public boolean isRequireAnnotations() {
			return requireAnnotations;
		}
	}
}
