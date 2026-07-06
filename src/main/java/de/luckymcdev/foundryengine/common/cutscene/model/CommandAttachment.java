package de.luckymcdev.foundryengine.common.cutscene.model;

import net.minecraft.nbt.CompoundTag;

/**
 * Attachment for executing server commands at a specific point in the cutscene.
 * Commands are executed on the server with appropriate permission checks.
 */
public class CommandAttachment extends CutsceneAttachment {
	public static final String TYPE = "command";

	private String command;
	private float delay; // Normalized delay after the 'at' point (optional, default 0)

	public CommandAttachment(float at, String command, float delay) {
		super(at, TYPE);
		this.command = command == null ? "" : command;
		this.delay = Math.max(0f, delay);
	}

	public static CommandAttachment fromNbt(CompoundTag tag) {
		float at = tag.getFloatOr("At", 0f);
		String command = tag.getStringOr("Command", "");
		float delay = tag.getFloatOr("Delay", 0f);
		return new CommandAttachment(at, command, delay);
	}

	@Override
	public CompoundTag toNbt() {
		CompoundTag tag = super.toNbt();
		tag.putString("Command", this.command);
		tag.putFloat("Delay", this.delay);
		return tag;
	}

	@Override
	public CutsceneAttachment copy() {
		return new CommandAttachment(this.at, this.command, this.delay);
	}

	@Override
	public String getDisplayName() {
		String cmdPreview = command.length() > 20 ? command.substring(0, 20) + "..." : command;
		return "Cmd: " + cmdPreview;
	}

	@Override
	public float getDuration() {
		return delay; // Commands are instant but can have a delay
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command == null ? "" : command;
	}

	public float getDelay() {
		return delay;
	}

	public void setDelay(float delay) {
		this.delay = Math.max(0f, delay);
	}

	/**
	 * Returns the effective trigger time (at + delay) in normalized form.
	 */
	public float getEffectiveAt() {
		return Math.min(1f, at + delay);
	}

	/**
	 * Returns the effective trigger tick based on cutscene length.
	 */
	public int getTriggerTick(float cutsceneLength) {
		return Math.round(getEffectiveAt() * cutsceneLength);
	}
}
