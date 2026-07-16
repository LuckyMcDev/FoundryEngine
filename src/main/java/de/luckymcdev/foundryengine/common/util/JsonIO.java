package de.luckymcdev.foundryengine.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.luckymcdev.foundryengine.common.Common;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonIO {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static JsonElement read(Path path) throws IOException {
		path = validate(path);
		try (Reader reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader);
		}
	}

	public static void write(JsonElement element, Path path) throws IOException {
		path = validate(path);
		Files.createDirectories(path.getParent());
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(element, writer);
		}
	}

	private static Path validate(Path path) {
		path = path.normalize().toAbsolutePath();
		if (!path.startsWith(Common.GAMEDIR)) {
			throw new SecurityException("Path " + path + " is outside the Minecraft directory " + Common.GAMEDIR);
		}
		return path;
	}
}
