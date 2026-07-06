package de.luckymcdev.foundryengine.common.event;

public class EventGroupHolder<T> {

	private final EventGroup<T> group = new EventGroup<>();

	public void register(EventCallback<T> callback) {
		group.add(callback);
	}

	public void post(T event) {
		group.post(event);
	}

	public void clear() {
		group.clear();
	}
}