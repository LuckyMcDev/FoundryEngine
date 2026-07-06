package de.luckymcdev.foundryengine.common.priority;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Generic Priority Enum for ordering and prioritization.
 * Lower ordinal = higher priority (HIGHEST loads/executes first).
 */
public enum Priority {
	HIGHEST(0),
	HIGH(100),
	NORMAL(500),
	LOW(900),
	LOWEST(1000);

	private final int value;

	Priority(int value) {
		this.value = value;
	}

	/**
	 * Get a comparator that sorts by priority (highest first).
	 * Usage: list.sort(Priority.comparator())
	 *
	 * @return comparator for sorting by priority
	 */
	public static Comparator<Priority> comparator() {
		return Comparator.comparingInt(Priority::getValue);
	}

	/**
	 * Get a comparator that sorts objects by their priority (highest first).
	 * Usage: entrypoints.sort(Priority.comparing(BundleEntrypoint::getPriority))
	 *
	 * @param keyExtractor function to extract priority from object
	 * @return comparator for sorting objects by priority
	 */
	public static <T> Comparator<T> comparing(java.util.function.Function<? super T, Priority> keyExtractor) {
		return Comparator.comparingInt(obj -> keyExtractor.apply(obj).value);
	}

	/**
	 * Get a comparator that sorts objects by priority in reverse (lowest first).
	 *
	 * @param keyExtractor function to extract priority from object
	 * @return reverse comparator for sorting objects by priority
	 */
	public static Comparator<Object> comparingReversed(Function<Object, Priority> keyExtractor) {
		return Comparator.comparingInt(obj -> keyExtractor.apply(obj).value).reversed();
	}

	/**
	 * Get the priority with the highest precedence (HIGHEST).
	 *
	 * @return HIGHEST priority
	 */
	public static Priority highest() {
		return HIGHEST;
	}

	/**
	 * Get the priority with the lowest precedence (LOWEST).
	 *
	 * @return LOWEST priority
	 */
	public static Priority lowest() {
		return LOWEST;
	}

	/**
	 * Get the default priority (NORMAL).
	 *
	 * @return NORMAL priority
	 */
	public static Priority normal() {
		return NORMAL;
	}

	/**
	 * Parse priority from string (case-insensitive).
	 *
	 * @param name priority name
	 * @return parsed Priority, or NORMAL if invalid
	 */
	public static Priority parse(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return NORMAL;
		}
	}

	/**
	 * Parse priority from string with fallback.
	 *
	 * @param name     priority name
	 * @param fallback fallback priority if parsing fails
	 * @return parsed Priority, or fallback if invalid
	 */
	public static Priority parse(String name, Priority fallback) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}

	/**
	 * Get the numeric value of this priority.
	 * Lower values = higher priority.
	 *
	 * @return numeric priority value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Check if this priority is higher than another.
	 *
	 * @param other priority to compare against
	 * @return true if this priority is higher (executes first)
	 */
	public boolean isHigherThan(Priority other) {
		return this.value < other.value;
	}

	/**
	 * Check if this priority is lower than another.
	 *
	 * @param other priority to compare against
	 * @return true if this priority is lower (executes last)
	 */
	public boolean isLowerThan(Priority other) {
		return this.value > other.value;
	}

	/**
	 * Check if this priority is the same as another.
	 *
	 * @param other priority to compare against
	 * @return true if priorities are equal
	 */
	public boolean isSameAs(Priority other) {
		return this.value == other.value;
	}

	/**
	 * Check if this priority is at least as high as another.
	 *
	 * @param other priority to compare against
	 * @return true if this >= other
	 */
	public boolean isAtLeast(Priority other) {
		return this.value <= other.value;
	}

	/**
	 * Check if this priority is at most as high as another.
	 *
	 * @param other priority to compare against
	 * @return true if this is greater than 'other' priority
	 */
	public boolean isAtMost(Priority other) {
		return this.value >= other.value;
	}

	@Override
	public String toString() {
		return name() + "(" + value + ")";
	}
}