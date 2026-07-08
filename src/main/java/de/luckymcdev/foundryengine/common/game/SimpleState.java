package de.luckymcdev.foundryengine.common.game;

public record SimpleState(String name, boolean active) implements GameState {

	public static final SimpleState LOBBY = new SimpleState("LOBBY", true);
	public static final SimpleState PLAYING = new SimpleState("PLAYING", true);
	public static final SimpleState FINISHED = new SimpleState("FINISHED", false);
	public static final SimpleState STOPPED = new SimpleState("STOPPED", false);

	@Override
	public boolean isActive() {
		return active;
	}
}
