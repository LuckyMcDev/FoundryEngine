package de.luckymcdev.foundryengine.common.priority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTest {

	@Test
	void values_AllDefined() {
		assertNotNull(Priority.HIGHEST);
		assertNotNull(Priority.HIGH);
		assertNotNull(Priority.NORMAL);
		assertNotNull(Priority.LOW);
		assertNotNull(Priority.LOWEST);
	}

	@Test
	void getValue_CorrectValues() {
		assertEquals(0, Priority.HIGHEST.getValue());
		assertEquals(100, Priority.HIGH.getValue());
		assertEquals(500, Priority.NORMAL.getValue());
		assertEquals(900, Priority.LOW.getValue());
		assertEquals(1000, Priority.LOWEST.getValue());
	}

	@Test
	void comparator_HighestFirst() {
		List<Priority> list = new ArrayList<>(List.of(Priority.LOW, Priority.HIGHEST, Priority.NORMAL, Priority.LOWEST, Priority.HIGH));
		list.sort(Priority.comparator());
		assertEquals(List.of(Priority.HIGHEST, Priority.HIGH, Priority.NORMAL, Priority.LOW, Priority.LOWEST), list);
	}

	@Test
	void comparing_SortsByPriority() {
		record P(String name, Priority priority) {
		}
		List<P> items = List.of(
			new P("low", Priority.LOW),
			new P("high", Priority.HIGH),
			new P("highest", Priority.HIGHEST)
		);
		List<P> sorted = new ArrayList<>(items);
		sorted.sort(Priority.comparing(P::priority));
		assertEquals("highest", sorted.get(0).name());
		assertEquals("high", sorted.get(1).name());
		assertEquals("low", sorted.get(2).name());
	}

	@Test
	void comparingReversed_LowestFirst() {
		record P(String name, Priority priority) {
		}
		List<P> items = List.of(
			new P("high", Priority.HIGH),
			new P("lowest", Priority.LOWEST),
			new P("highest", Priority.HIGHEST)
		);
		List<P> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparingInt((P p) -> p.priority().getValue()).reversed());
		assertEquals("lowest", sorted.get(0).name());
		assertEquals("highest", sorted.get(2).name());
	}

	@Test
	void highest_Lowest_Normal_StaticMethods() {
		assertSame(Priority.HIGHEST, Priority.highest());
		assertSame(Priority.LOWEST, Priority.lowest());
		assertSame(Priority.NORMAL, Priority.normal());
	}

	@Test
	void isHigherThan_True() {
		assertTrue(Priority.HIGHEST.isHigherThan(Priority.HIGH));
		assertTrue(Priority.HIGH.isHigherThan(Priority.NORMAL));
	}

	@Test
	void isHigherThan_False() {
		assertFalse(Priority.NORMAL.isHigherThan(Priority.HIGH));
		assertFalse(Priority.LOW.isHigherThan(Priority.LOW));
	}

	@Test
	void isLowerThan_True() {
		assertTrue(Priority.LOWEST.isLowerThan(Priority.LOW));
		assertTrue(Priority.NORMAL.isLowerThan(Priority.HIGH));
	}

	@Test
	void isLowerThan_False() {
		assertFalse(Priority.HIGH.isLowerThan(Priority.NORMAL));
	}

	@Test
	void isSameAs_True() {
		assertTrue(Priority.NORMAL.isSameAs(Priority.NORMAL));
	}

	@Test
	void isSameAs_False() {
		assertFalse(Priority.NORMAL.isSameAs(Priority.HIGH));
	}

	@Test
	void isAtLeast_True() {
		assertTrue(Priority.HIGHEST.isAtLeast(Priority.HIGH));
		assertTrue(Priority.NORMAL.isAtLeast(Priority.NORMAL));
	}

	@Test
	void isAtLeast_False() {
		assertFalse(Priority.LOW.isAtLeast(Priority.HIGH));
	}

	@Test
	void isAtMost_True() {
		assertTrue(Priority.LOWEST.isAtMost(Priority.LOW));
		assertTrue(Priority.NORMAL.isAtMost(Priority.NORMAL));
	}

	@Test
	void isAtMost_False() {
		assertFalse(Priority.HIGH.isAtMost(Priority.NORMAL));
	}

	@ParameterizedTest
	@CsvSource({
		"HIGHEST, HIGHEST",
		"normal, NORMAL",
		"Low, LOW",
		"lowest, LOWEST",
		"HIGH, HIGH"
	})
	void parse_ValidInput(String input, Priority expected) {
		assertEquals(expected, Priority.parse(input));
	}

	@Test
	void parse_InvalidInput_ReturnsNormal() {
		assertSame(Priority.NORMAL, Priority.parse("INVALID"));
		assertSame(Priority.NORMAL, Priority.parse("something_else"));
		assertSame(Priority.NORMAL, Priority.parse(""));
	}

	@Test
	void parse_InvalidInputWithFallback_ReturnsFallback() {
		assertSame(Priority.LOWEST, Priority.parse("INVALID", Priority.LOWEST));
		assertSame(Priority.HIGH, Priority.parse("INVALID", Priority.HIGH));
	}

	@Test
	void parse_ValidInputWithFallback_ReturnsParsed() {
		assertSame(Priority.HIGHEST, Priority.parse("HIGHEST", Priority.LOW));
	}

	@Test
	void toString_ContainsNameAndValue() {
		assertEquals("NORMAL(500)", Priority.NORMAL.toString());
		assertEquals("HIGHEST(0)", Priority.HIGHEST.toString());
	}
}
