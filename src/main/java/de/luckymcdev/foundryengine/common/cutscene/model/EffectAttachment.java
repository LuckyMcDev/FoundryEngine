package de.luckymcdev.foundryengine.common.cutscene.model;

import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import net.minecraft.nbt.CompoundTag;

/**
 * Attachment for screen effects (post-processing shaders).
 * Handles phased transitions: intro, hold, and outro with configurable durations and easing.
 */
public class EffectAttachment extends CutsceneAttachment {
	public static final String TYPE = "effect";

	private String effectName;
	private float introDuration;
	private float holdDuration;
	private float outroDuration;
	private String lerpType;

	public EffectAttachment(float at, String effectName, float introDuration, float holdDuration, float outroDuration, String lerpType) {
		super(at, TYPE);
		this.effectName = effectName == null ? "none" : effectName;
		this.introDuration = Math.max(0.0f, introDuration);
		this.holdDuration = Math.max(0.0f, holdDuration);
		this.outroDuration = Math.max(0.0f, outroDuration);
		this.lerpType = (lerpType == null || lerpType.isBlank()) ? LerpType.LINEAR.name() : lerpType;
	}

	public static EffectAttachment fromNbt(CompoundTag tag) {
		float at = tag.getFloatOr("At", 0.0f);
		String effectName = tag.getStringOr("EffectName", "none");
		float intro = tag.getFloatOr("IntroDuration", -1.0f);
		float hold = tag.getFloatOr("HoldDuration", -1.0f);
		float outro = tag.getFloatOr("OutroDuration", -1.0f);
		String lerp = tag.getStringOr("LerpType", LerpType.LINEAR.name());

		if (intro < 0.0f || hold < 0.0f || outro < 0.0f) {
			int oldIntro = tag.getIntOr("Intro", 0);
			int oldHold = tag.getIntOr("Hold", 0);
			int oldOutro = tag.getIntOr("Outro", 0);
			float defaultLen = 60.0f;
			intro = oldIntro / defaultLen;
			hold = oldHold / defaultLen;
			outro = oldOutro / defaultLen;
		}

		return new EffectAttachment(at, effectName, intro, hold, outro, lerp);
	}

	@Override
	public CompoundTag toNbt() {
		CompoundTag tag = super.toNbt();
		tag.putString("EffectName", this.effectName);
		tag.putFloat("IntroDuration", this.introDuration);
		tag.putFloat("HoldDuration", this.holdDuration);
		tag.putFloat("OutroDuration", this.outroDuration);
		tag.putString("LerpType", this.lerpType);
		return tag;
	}

	@Override
	public CutsceneAttachment copy() {
		return new EffectAttachment(this.at, this.effectName, this.introDuration, this.holdDuration, this.outroDuration, this.lerpType);
	}

	@Override
	public String getDisplayName() {
		return effectName + " [effect]";
	}

	@Override
	public float getDuration() {
		return introDuration + holdDuration + outroDuration;
	}

	public String getEffectName() {
		return effectName;
	}

	public void setEffectName(String effectName) {
		this.effectName = effectName == null ? "none" : effectName;
	}

	public float getIntroDuration() {
		return introDuration;
	}

	public void setIntroDuration(float introDuration) {
		this.introDuration = Math.max(0.0f, introDuration);
	}

	public float getHoldDuration() {
		return holdDuration;
	}

	public void setHoldDuration(float holdDuration) {
		this.holdDuration = Math.max(0.0f, holdDuration);
	}

	public float getOutroDuration() {
		return outroDuration;
	}

	public void setOutroDuration(float outroDuration) {
		this.outroDuration = Math.max(0.0f, outroDuration);
	}

	public String getLerpType() {
		return lerpType;
	}

	public void setLerpType(String lerpType) {
		this.lerpType = (lerpType == null || lerpType.isBlank()) ? LerpType.LINEAR.name() : lerpType;
	}

	public int getIntroTicks(float cutsceneLength) {
		return Math.max(0, Math.round(introDuration * cutsceneLength));
	}

	public int getHoldTicks(float cutsceneLength) {
		return Math.max(0, Math.round(holdDuration * cutsceneLength));
	}

	public int getOutroTicks(float cutsceneLength) {
		return Math.max(0, Math.round(outroDuration * cutsceneLength));
	}
}
