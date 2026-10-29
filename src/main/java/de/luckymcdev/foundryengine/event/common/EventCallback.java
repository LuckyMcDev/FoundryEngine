package de.luckymcdev.foundryengine.event.common;

@FunctionalInterface
public interface EventCallback<T> {
	void execute(T event);
}