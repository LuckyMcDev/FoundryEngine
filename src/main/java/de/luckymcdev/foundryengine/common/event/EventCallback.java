package de.luckymcdev.foundryengine.common.event;

@FunctionalInterface
public interface EventCallback<T> {
	void execute(T event);
}