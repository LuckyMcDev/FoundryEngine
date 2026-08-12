package de.luckymcdev.foundryengine.common.service;

public record EngineServiceResult(int exitCode, String stdout, String stderr) {

	public boolean success() {
		return exitCode == 0;
	}
}