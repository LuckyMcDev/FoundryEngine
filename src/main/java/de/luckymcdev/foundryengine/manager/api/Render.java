package de.luckymcdev.foundryengine.manager.api;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * A client manager that contributes to the level render pass.
 */
public interface Render extends EngineManager {
	//? if >=26.2 {
	/*void render(RenderLevelStageEvent.AfterLevel event, net.minecraft.client.renderer.SubmitNodeStorage submitNodes);*/
	//?} else {
	void render(RenderLevelStageEvent.AfterLevel event);
	//?}
}
