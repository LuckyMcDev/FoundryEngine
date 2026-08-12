package de.luckymcdev.foundryengine.common.service;

import de.luckymcdev.foundryengine.common.Common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PakkuService extends EngineService {

	CompletableFuture<EngineServiceResult> list();
	CompletableFuture<EngineServiceResult> add(String... projects);
	CompletableFuture<EngineServiceResult> remove(String... projects);
	CompletableFuture<EngineServiceResult> update(String... projects);
	CompletableFuture<EngineServiceResult> updateAll();
	CompletableFuture<EngineServiceResult> inspect(String... projects);
	CompletableFuture<EngineServiceResult> status();
	CompletableFuture<EngineServiceResult> init();
	CompletableFuture<EngineServiceResult> importModpack(String path);
	CompletableFuture<EngineServiceResult> export(String... options);
	CompletableFuture<EngineServiceResult> sync();
	CompletableFuture<EngineServiceResult> fetch();

	class Default extends CliEngineService implements PakkuService {

		private final Path jarPath;
		private final Path workingDirectory;

		public Default() {
			super("java");
			this.jarPath = Common.PAKKU.resolve("pakku.jar");
			this.workingDirectory = Common.DIRECTORY;
			if (!jarPath.toFile().exists()) {
				throw new IllegalStateException("pakku.jar not found at " + jarPath);
			}
		}

		@Override
		public String name() {
			return "pakku";
		}

		@Override
		public boolean isAvailable() {
			return jarPath.toFile().exists();
		}

		@Override
		public CompletableFuture<EngineServiceResult> execute(String... args) {
			List<String> command = new ArrayList<>();
			command.add("java");
			command.add("--enable-native-access=ALL-UNNAMED");
			command.add("-jar");
			command.add(jarPath.toString());
			command.addAll(Arrays.asList(args));

			return CompletableFuture.supplyAsync(() -> {
				try {
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.directory(workingDirectory.toFile());
					pb.redirectErrorStream(false);
					Process process = pb.start();

					String stdout = new String(process.getInputStream().readAllBytes());
					String stderr = new String(process.getErrorStream().readAllBytes());
					int exitCode = process.waitFor();

					return new EngineServiceResult(exitCode, stdout.strip(), stderr.strip());
				} catch (IOException | InterruptedException e) {
					return new EngineServiceResult(-1, "", e.getMessage());
				}
			});
		}

		@Override
		public CompletableFuture<EngineServiceResult> list() {
			return execute("ls");
		}

		@Override
		public CompletableFuture<EngineServiceResult> add(String... projects) {
			String[] args = new String[projects.length + 1];
			args[0] = "add";
			System.arraycopy(projects, 0, args, 1, projects.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> remove(String... projects) {
			String[] args = new String[projects.length + 1];
			args[0] = "rm";
			System.arraycopy(projects, 0, args, 1, projects.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> update(String... projects) {
			String[] args = new String[projects.length + 1];
			args[0] = "update";
			System.arraycopy(projects, 0, args, 1, projects.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> updateAll() {
			return execute("update", "--all");
		}

		@Override
		public CompletableFuture<EngineServiceResult> inspect(String... projects) {
			String[] args = new String[projects.length + 1];
			args[0] = "insp";
			System.arraycopy(projects, 0, args, 1, projects.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> status() {
			return execute("status");
		}

		@Override
		public CompletableFuture<EngineServiceResult> init() {
			return execute("init");
		}

		@Override
		public CompletableFuture<EngineServiceResult> importModpack(String path) {
			return execute("import", path);
		}

		@Override
		public CompletableFuture<EngineServiceResult> export(String... options) {
			String[] args = new String[options.length + 1];
			args[0] = "export";
			System.arraycopy(options, 0, args, 1, options.length);
			return execute(args);
		}

		@Override
		public CompletableFuture<EngineServiceResult> sync() {
			return execute("sync");
		}

		@Override
		public CompletableFuture<EngineServiceResult> fetch() {
			return execute("fetch");
		}
	}
}