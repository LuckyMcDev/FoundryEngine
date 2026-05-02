package de.luckymcdev.foundryengine.common.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventGroup<T> {
    private final List<EventCallback<T>> listeners = new CopyOnWriteArrayList<>();

    public void add(EventCallback<T> callback) {
        listeners.add(callback);
    }

    public void post(T event) {
        for (EventCallback<T> listener : listeners) {
            listener.execute(event);
        }
    }

    public void clear() {
        this.listeners.clear();
    }
}