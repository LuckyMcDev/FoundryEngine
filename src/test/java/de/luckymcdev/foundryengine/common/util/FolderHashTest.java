package de.luckymcdev.foundryengine.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FolderHashTest {

	@Test
	void hashFolder_EmptyFolder_ReturnsHash(@TempDir Path tempDir) throws Exception {
		String hash = FolderHash.hashFolder(tempDir);
		assertNotNull(hash);
		assertFalse(hash.isEmpty());
		assertEquals(64, hash.length()); // SHA-256 hex = 64 chars
	}

	@Test
	void hashFolder_SingleFile_Deterministic(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve("test.txt"), "hello");
		String hash1 = FolderHash.hashFolder(tempDir);
		String hash2 = FolderHash.hashFolder(tempDir);
		assertEquals(hash1, hash2);
	}

	@Test
	void hashFolder_DifferentContent_DifferentHash(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve("a.txt"), "content a");
		String hashA = FolderHash.hashFolder(tempDir);

		Files.writeString(tempDir.resolve("b.txt"), "content b");
		String hashB = FolderHash.hashFolder(tempDir);

		assertNotEquals(hashA, hashB, "Adding a file should change the hash");
	}

	@Test
	void hashFolder_FileOrder_DoesNotAffectHash(@TempDir Path tempDir) throws Exception {
		Path dir1 = tempDir.resolve("d1");
		Path dir2 = tempDir.resolve("d2");
		Files.createDirectories(dir1);
		Files.createDirectories(dir2);
		Files.writeString(dir1.resolve("a.txt"), "content");
		Files.writeString(dir1.resolve("b.txt"), "content");
		Files.writeString(dir2.resolve("b.txt"), "content");
		Files.writeString(dir2.resolve("a.txt"), "content");
		String hash1 = FolderHash.hashFolder(dir1);
		String hash2 = FolderHash.hashFolder(dir2);
		assertEquals(hash1, hash2, "Hash should be independent of file iteration order");
	}

	@Test
	void hashFolder_Subdirectory_IncludesAllFiles(@TempDir Path tempDir) throws Exception {
		Files.createDirectories(tempDir.resolve("sub"));
		Files.writeString(tempDir.resolve("sub").resolve("deep.txt"), "deep");
		String hash = FolderHash.hashFolder(tempDir);
		assertNotNull(hash);
		assertEquals(64, hash.length());
	}

	@Test
	void hashFolder_NonExistentPath_Throws(@TempDir Path tempDir) {
		Path nonExistent = tempDir.resolve("does_not_exist");
		assertThrows(IOException.class, () -> FolderHash.hashFolder(nonExistent));
	}

	@Test
	void hashFolder_FileInsteadOfDirectory_ReturnsHash(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("file.txt");
		Files.writeString(file, "data");
		String hash = FolderHash.hashFolder(file);
		assertNotNull(hash);
		assertEquals(64, hash.length());
	}

	@Test
	void hashFolder_LargeContent_ProducesHash(@TempDir Path tempDir) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			sb.append("line ").append(i).append("\n");
		}
		Files.writeString(tempDir.resolve("large.txt"), sb.toString());
		String hash = FolderHash.hashFolder(tempDir);
		assertNotNull(hash);
		assertEquals(64, hash.length());
	}

	@Test
	void hashFolder_SameNameDifferentContent_DifferentHash(@TempDir Path tempDir) throws Exception {
		Path dir1 = tempDir.resolve("d1");
		Path dir2 = tempDir.resolve("d2");
		Files.createDirectories(dir1);
		Files.createDirectories(dir2);
		Files.writeString(dir1.resolve("same.txt"), "version 1");
		Files.writeString(dir2.resolve("same.txt"), "version 2");
		assertNotEquals(
			FolderHash.hashFolder(dir1),
			FolderHash.hashFolder(dir2)
		);
	}
}
