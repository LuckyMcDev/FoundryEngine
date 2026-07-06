package de.luckymcdev.foundryengine.common.cutscene.util;

import de.luckymcdev.foundryengine.common.easing.Easing;
import net.minecraft.util.Mth;

public enum LerpType implements LerpOperation<Float> {
	LINEAR(Easing.LINEAR),

	SINE_IN(Easing.SINE_IN),
	SINE_OUT(Easing.SINE_OUT),
	SINE_IN_OUT(Easing.SINE_IN_OUT),

	CUBIC_IN(Easing.CUBIC_IN),
	CUBIC_OUT(Easing.CUBIC_OUT),
	CUBIC_IN_OUT(Easing.CUBIC_IN_OUT),

	QUINT_IN(Easing.QUINTIC_IN),
	QUINT_OUT(Easing.QUINTIC_OUT),
	QUINT_IN_OUT(Easing.QUINTIC_IN_OUT),

	BOUNCE_OUT(Easing.BOUNCE_OUT),
	BOUNCE_IN(Easing.BOUNCE_IN),
	BOUNCE_IN_OUT(Easing.BOUNCE_IN_OUT);

	private final Easing easing;

	LerpType(Easing easing) {
		this.easing = easing;
	}

	public static LerpType fromString(String name) {
		if (name == null) {
			return LINEAR;
		}
		for (LerpType lerpType : values()) {
			if (lerpType.name().equalsIgnoreCase(name)) {
				return lerpType;
			}
			if (lerpType.easing.name.equalsIgnoreCase(name)) {
				return lerpType;
			}
		}
		return LINEAR;
	}

	@Override
	public Float compute(Float t) {
		if (t == null) {
			return 0.0f;
		}
		float clamped = Mth.clamp(t, 0.0f, 1.0f);
		return easing.ease(clamped, 0.0f, 1.0f, 1.0f);
	}
}
