package de.luckymcdev.foundryengine.common.network.packets.editor;

import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum CutsceneAction implements StringRepresentable {
	ADD("add"),
	REMOVE("remove"),
	PLAY("play"),
	CANCEL("cancel");

	private static final Map<String, CutsceneAction> BY_NAME = Arrays.stream(values())
		.collect(Collectors.toMap(CutsceneAction::name, a -> a));

	private final String serializedName;

	CutsceneAction(String serializedName) {
		this.serializedName = serializedName;
	}

	public static CutsceneAction fromString(String name) {
		return name == null ? null : BY_NAME.get(name);
	}

	public static CutsceneAction fromString(String name, CutsceneAction fallback) {
		var result = fromString(name);
		return result != null ? result : fallback;
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}
}
