package de.luckymcdev.foundryengine.service.common;

public record EngineServiceResult(int exitCode, String stdout, String stderr) {

	public boolean success() {
		return exitCode == 0;
	}
}