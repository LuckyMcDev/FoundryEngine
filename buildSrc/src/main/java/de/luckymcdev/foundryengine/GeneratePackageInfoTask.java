package de.luckymcdev.foundryengine;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class GeneratePackageInfoTask extends DefaultTask {

	@InputDirectory
	public abstract DirectoryProperty getSourceDir();

	@TaskAction
	public void execute() throws IOException {
		File srcDir = getSourceDir().get().getAsFile();
		Path srcPath = srcDir.toPath();

		Files.walk(srcPath)
			.filter(Files::isDirectory)
			.forEach(dirPath -> {
				File dir = dirPath.toFile();
				File[] files = dir.listFiles();
				if (files == null) {
					return;
				}

				boolean hasJava = false;
				for (File f : files) {
					if (f.getName().endsWith(".java")) {
						hasJava = true;
						break;
					}
				}
				if (!hasJava) {
					return;
				}

				File pkgFile = new File(dir, "package-info.java");
				if (pkgFile.exists()) {
					return;
				}

				String pkg = srcPath.relativize(dirPath).toString().replace(File.separator, ".");
				String desc = formatPackageDescription(pkg);

				String content = """
					/**
					 * %s
					 */
					@NullMarked
					package %s;
					
					import org.jspecify.annotations.NullMarked;
					""".formatted(desc, pkg);

				try {
					Files.writeString(pkgFile.toPath(), content);
					getLogger().lifecycle("Generated: {}", pkgFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
	}

	private String formatPackageDescription(String pkg) {
		String[] segments = pkg.split("\\.");
		int foundryIdx = -1;
		for (int i = 0; i < segments.length; i++) {
			if ("foundryengine".equals(segments[i])) {
				foundryIdx = i;
				break;
			}
		}

		if (foundryIdx >= 0 && foundryIdx < segments.length - 1) {
			StringBuilder sb = new StringBuilder();
			for (int i = foundryIdx + 1; i < segments.length; i++) {
				if (i > foundryIdx + 1) {
					sb.append(" - ");
				}
				sb.append(segments[i].replaceAll("([a-z])([A-Z])", "$1 $2"));
			}
			return sb.toString();
		}
		return pkg;
	}
}