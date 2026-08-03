package de.luckymcdev.foundryengine.dsl;

import me.modmuss50.mpp.ReleaseType;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * Publishing configuration (Maven, Modrinth/CurseForge/GitHub via mod-publish-plugin).
 */
public abstract class PublishingExtension {

	@Inject
	public PublishingExtension() {
		getGithubRepository().convention("LuckyMcDev/FoundryEngine");
		getGithubCommitish().convention("master");
		getReleaseType().convention(ReleaseType.ALPHA);
	}

	/**
	 * {@code owner/repo} used for the GitHub release target.
	 */
	public abstract Property<String> getGithubRepository();

	/**
	 * Branch or tag the release targets.
	 */
	public abstract Property<String> getGithubCommitish();

	/**
	 * Release type for mod-publish-plugin.
	 */
	public abstract Property<ReleaseType> getReleaseType();
}