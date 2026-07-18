package de.luckymcdev.foundryengine.common;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonTest {

	@Test
	void id_ReturnsNamespacedIdentifier() {
		Identifier result = Common.id("test_path");
		assertEquals("foundryengine", result.getNamespace());
		assertEquals("test_path", result.getPath());
	}

	@Test
	void id_WithPath_NoLeadingSlash() {
		Identifier result = Common.id("path/to/something");
		assertEquals("path/to/something", result.getPath());
	}

	@Test
	void mId_ReturnsMinecraftNamespace() {
		Identifier result = Common.mId("stone");
		assertEquals("minecraft", result.getNamespace());
		assertEquals("stone", result.getPath());
	}

	@Test
	void id_WithCustomNamespace() {
		Identifier result = Common.id("custom_ns", "some/path");
		assertEquals("custom_ns", result.getNamespace());
		assertEquals("some/path", result.getPath());
	}

	@Test
	void getFileContent_ExistingFile_ReturnsContent(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("test.txt");
		Files.writeString(file, "Hello, World!");
		String content = Common.getFileContent(file);
		assertEquals("Hello, World!", content);
	}

	@Test
	void getFileContent_MissingFile_ReturnsEmpty(@TempDir Path tempDir) {
		Path missing = tempDir.resolve("nonexistent.txt");
		String content = Common.getFileContent(missing);
		assertEquals("", content);
	}

	@Test
	void getFileContent_EmptyFile_ReturnsEmpty(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("empty.txt");
		Files.createFile(file);
		String content = Common.getFileContent(file);
		assertEquals("", content);
	}

	@Test
	void getFileContent_MultilineFile(@TempDir Path tempDir) throws IOException {
		Path file = tempDir.resolve("multi.txt");
		Files.writeString(file, "line1\nline2\nline3");
		String content = Common.getFileContent(file);
		assertEquals("line1\nline2\nline3", content);
	}

	@Test
	void getFileContent_NullPath_ReturnsEmpty() {
		String content = Common.getFileContent(null);
		assertEquals("", content);
	}

	@Test
	void modid_Constant() {
		assertEquals("foundryengine", Common.MODID);
	}

	@Test
	void modname_Constant() {
		assertEquals("FoundryEngine", Common.MODNAME);
	}

	@Test
	void postEvent_DoesNotThrow() {
		var event = new net.neoforged.bus.api.Event() {
		};
		assertDoesNotThrow(() -> Common.post(event));
	}
}
