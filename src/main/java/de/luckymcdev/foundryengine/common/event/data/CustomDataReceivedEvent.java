package de.luckymcdev.foundryengine.common.event.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class CustomDataReceivedEvent extends Event {
	private final Player player;
	private final String id;
	private final CompoundTag data;

	public CustomDataReceivedEvent(Player player, String id, CompoundTag data) {
		this.player = player;
		this.id = id;
		this.data = data;
	}

	public Player getPlayer() {
		return player;
	}

	public String getId() {
		return id;
	}

	public CompoundTag getData() {
		return data;
	}
}
