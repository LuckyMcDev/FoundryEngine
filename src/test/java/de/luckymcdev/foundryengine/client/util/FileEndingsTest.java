package de.luckymcdev.foundryengine.client.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileEndingsTest {

	@Test
	void getExtension_StandardFile() {
		String ext = invokeGetExtension("test.java");
		assertEquals("java", ext);
	}

	@Test
	void getExtension_FileWithPath() {
		String ext = invokeGetExtension("src/main/java/Test.java");
		assertEquals("java", ext);
	}

	@Test
	void getExtension_MultipleDots() {
		String ext = invokeGetExtension("archive.tar.gz");
		assertEquals("gz", ext);
	}

	@Test
	void getExtension_NoExtension() {
		String ext = invokeGetExtension("README");
		assertNull(ext);
	}

	@Test
	void getExtension_Null() {
		String ext = invokeGetExtension(null);
		assertNull(ext);
	}

	@Test
	void getExtension_EmptyString() {
		String ext = invokeGetExtension("");
		assertNull(ext);
	}

	@Test
	void getExtension_DotOnly() {
		String ext = invokeGetExtension(".");
		assertEquals("", ext);
	}

	@Test
	void getFileIcon_KnownExtension() {
		String icon = FileEndings.getFileIcon("Test.java");
		assertNotNull(icon);
	}

	@Test
	void getFileIcon_UnknownExtension() {
		String icon = FileEndings.getFileIcon("unknown.xyz");
		assertNotNull(icon);
	}

	@Test
	void getFileIcon_NullFileName() {
		String icon = FileEndings.getFileIcon(null);
		assertNotNull(icon);
	}

	private String invokeGetExtension(String fileName) {
		try {
			var method = FileEndings.class.getDeclaredMethod("getExtension", String.class);
			method.setAccessible(true);
			return (String) method.invoke(null, fileName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
