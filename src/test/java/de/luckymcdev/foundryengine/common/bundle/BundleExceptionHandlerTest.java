package de.luckymcdev.foundryengine.common.bundle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests BundleExceptionHandler.handle() — verifies it doesn't throw.
 * Server broadcast is skipped in test env (no server), so only logging is tested.
 */
class BundleExceptionHandlerTest {

	@Test
	void handle_RuntimeException_DoesNotThrow() {
		assertDoesNotThrow(() ->
			BundleExceptionHandler.handle("Test context", new RuntimeException("Test error"))
		);
	}

	@Test
	void handle_IOException_DoesNotThrow() {
		assertDoesNotThrow(() ->
			BundleExceptionHandler.handle("IO context", new java.io.IOException("File not found"))
		);
	}

	@Test
	void handle_NullMessageException_DoesNotThrow() {
		assertDoesNotThrow(() ->
			BundleExceptionHandler.handle("Null context", new NullPointerException())
		);
	}

	@Test
	void handle_EmptyContext_DoesNotThrow() {
		assertDoesNotThrow(() ->
			BundleExceptionHandler.handle("", new Exception("error"))
		);
	}

	@Test
	void handle_WrappedException_DoesNotThrow() {
		assertDoesNotThrow(() ->
			BundleExceptionHandler.handle("Wrapped", new Exception(new RuntimeException("cause")))
		);
	}
}
