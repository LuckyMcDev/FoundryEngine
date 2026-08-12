package de.luckymcdev.foundryengine.common.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CliEngineService implements EngineService {

	private final String executable;

	protected CliEngineService(String executable) {
		this.executable = executable;
	}

	@Override
	public boolean isAvailable() {
		try {
			Process process = new ProcessBuilder(executable, "--version")
				.redirectErrorStream(true)
				.start();
			return process.waitFor() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	@Override
	public CompletableFuture<EngineServiceResult> execute(String... args) {
		return CompletableFuture.supplyAsync(() -> {
			List<String> command = new ArrayList<>();
			command.add(executable);
			command.addAll(Arrays.asList(args));

			try {
				Process process = new ProcessBuilder(command)
					.redirectErrorStream(false)
					.start();

				String stdout = new String(process.getInputStream().readAllBytes());
				String stderr = new String(process.getErrorStream().readAllBytes());
				int exitCode = process.waitFor();

				return new EngineServiceResult(exitCode, stdout.strip(), stderr.strip());
			} catch (IOException | InterruptedException e) {
				return new EngineServiceResult(-1, "", e.getMessage());
			}
		});
	}
}