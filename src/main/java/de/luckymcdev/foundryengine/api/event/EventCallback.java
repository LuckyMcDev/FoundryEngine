package de.luckymcdev.foundryengine.api.event;

@FunctionalInterface
public interface EventCallback<T> {
    void execute(T event);
}