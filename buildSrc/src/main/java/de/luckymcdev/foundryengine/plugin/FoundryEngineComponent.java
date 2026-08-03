package de.luckymcdev.foundryengine.plugin;

import org.gradle.api.Project;

/**
 * A single slice of the FoundryEngine build plugin. Each component is applied by
 * {@link FoundryEnginePlugin} in a fixed order and only touches its own concern.
 */
public interface FoundryEngineComponent {

	void apply(Project project);
}