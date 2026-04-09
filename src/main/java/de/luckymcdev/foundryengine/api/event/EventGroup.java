package de.luckymcdev.foundryengine.api.event;

import java.util.ArrayList;
import java.util.List;

public class EventGroup<T> {
    private final List<EventCallback<T>> listeners = new ArrayList<>();

    public void add(EventCallback<T> callback) {
        listeners.add(callback);
    }

    public void post(T event) {
        for (EventCallback<T> listener : listeners) {
            listener.execute(event);
        }
    }
}