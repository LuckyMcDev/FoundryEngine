package de.luckymcdev.foundryengine.manager.api;

/**
 * Stable keys for built-in managers.
 */
public enum ManagerKey {
	NETWORK("network"),
	SAVED_DATA("savedData"),
	ENGINE_SERVICE("engineService"),
	SCRIPT("script"),
	BUNDLE("bundle"),
	GAME("game"),
	GAME_CLIENT("gameClient"),
	GAME_STAGE("gameStage"),
	STAGE_TABLE("stageTable"),
	AREA("area"),
	CUTSCENE("cutscene"),
	CUTSCENE_SESSION("cutsceneSession"),
	WAYPOINT("waypoint"),
	DIALOGUE("dialogue"),
	IMGUI("imgui"),
	EDITOR("editor"),
	PARTICLE("particle"),
	CLIENT_WAYPOINT("clientWaypoint"),
	CLIENT_AREA("clientArea"),
	MESH("mesh"),
	OBJ_MODEL("objModel"),
	CLIENT_CUTSCENE("clientCutscene"),
	POST_EFFECT("postEffect"),
	SKYBOX("skybox"),
	CLIENT_DIALOGUE("clientDialogue"),
	ITEM_COMMAND("itemCommand"),
	HOTKEY("hotkey"),
	TOOLTIP("tooltip");

	private final String id;

	ManagerKey(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}
}
