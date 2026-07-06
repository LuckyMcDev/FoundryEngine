package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.util.ErrorHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventGroup<T> {

	private final List<EventCallback<T>> listeners = new CopyOnWriteArrayList<>();

	public void add(EventCallback<T> callback) {
		listeners.add(callback);
	}

	public void post(T event) {
		for (EventCallback<T> listener : listeners) {
			try {
				listener.execute(event);
			} catch (Throwable e) {
				ErrorHandler.handleScriptError("Event callback", e);
			}
		}
	}

	public void clear() {
		this.listeners.clear();
	}
}