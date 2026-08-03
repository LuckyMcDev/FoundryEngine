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
public record MixinConfigSpec(String name, String packageName, boolean required, String minVersion, String compatibilityLevel, List<String> mixins, List<String> client, List<String> server, List<String> requiredFeatures, String parent, String target, int priority, int mixinPriority, boolean setSourceFile, String refmap, boolean verbose, String plugin, InjectorsSpec injectors, OverwritesSpec overwrites) implements Serializable {

	private static final long serialVersionUID = 2L;

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

	@Override
	@Input
	public String name() {
		return name;
	}

	@Override
	@Input
	public String packageName() {
		return packageName;
	}

	@Override
	@Input
	public boolean required() {
		return required;
	}

	@Override
	@Input
	public String minVersion() {
		return minVersion;
	}

	@Override
	@Input
	public String compatibilityLevel() {
		return compatibilityLevel;
	}

	@Override
	@Input
	public List<String> mixins() {
		return mixins;
	}

	@Override
	@Input
	public List<String> client() {
		return client;
	}

	@Override
	@Input
	public List<String> server() {
		return server;
	}

	@Override
	@Input
	public List<String> requiredFeatures() {
		return requiredFeatures;
	}

	@Override
	@Input
	@Optional
	public String parent() {
		return parent;
	}

	@Override
	@Input
	@Optional
	public String target() {
		return target;
	}

	@Override
	@Input
	public int priority() {
		return priority;
	}

	@Override
	@Input
	public int mixinPriority() {
		return mixinPriority;
	}

	@Override
	@Input
	public boolean setSourceFile() {
		return setSourceFile;
	}

	@Override
	@Input
	@Optional
	public String refmap() {
		return refmap;
	}

	@Override
	@Input
	public boolean verbose() {
		return verbose;
	}

	@Override
	@Input
	@Optional
	public String plugin() {
		return plugin;
	}

	@Override
	@Nested
	public InjectorsSpec injectors() {
		return injectors;
	}

	@Override
	@Nested
	public OverwritesSpec overwrites() {
		return overwrites;
	}

	/**
	 * Serialisable injectors options.
	 */
	public record InjectorsSpec(int defaultRequire, String defaultGroup, String namespace, List<String> injectionPoints, List<String> dynamicSelectors, int maxShiftBy) implements Serializable {

		private static final long serialVersionUID = 1L;

		public InjectorsSpec(int defaultRequire, String defaultGroup, String namespace,
		                     List<String> injectionPoints, List<String> dynamicSelectors, int maxShiftBy) {
			this.defaultRequire = defaultRequire;
			this.defaultGroup = defaultGroup;
			this.namespace = namespace;
			this.injectionPoints = new ArrayList<>(injectionPoints);
			this.dynamicSelectors = new ArrayList<>(dynamicSelectors);
			this.maxShiftBy = maxShiftBy;
		}

		@Override
		@Input
		public int defaultRequire() {
			return defaultRequire;
		}

		@Override
		@Input
		@Optional
		public String defaultGroup() {
			return defaultGroup;
		}

		@Override
		@Input
		@Optional
		public String namespace() {
			return namespace;
		}

		@Override
		@Input
		public List<String> injectionPoints() {
			return injectionPoints;
		}

		@Override
		@Input
		public List<String> dynamicSelectors() {
			return dynamicSelectors;
		}

		@Override
		@Input
		public int maxShiftBy() {
			return maxShiftBy;
		}
	}

	/**
	 * Serialisable overwrites options.
	 */
	public record OverwritesSpec(boolean conformVisibility, boolean requireAnnotations) implements Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		@Input
		public boolean conformVisibility() {
			return conformVisibility;
		}

		@Override
		@Input
		public boolean requireAnnotations() {
			return requireAnnotations;
		}
	}
}
