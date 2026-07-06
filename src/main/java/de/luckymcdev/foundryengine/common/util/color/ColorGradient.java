package de.luckymcdev.foundryengine.common.util.color;

import de.luckymcdev.foundryengine.common.easing.Easing;

import java.util.ArrayList;
import java.util.List;

public class ColorGradient {
	private final List<GradientStep> steps = new ArrayList<>();

	public ColorGradient addStep(Color color, float pos, Easing easing) {
		steps.add(new GradientStep(color, pos, easing));
		steps.sort((a, b) -> Float.compare(a.pos, b.pos));
		return this;
	}

	public Color getColor(float progress) {
		if (steps.isEmpty()) {
			return Color.WHITE;
		}
		if (steps.size() == 1 || progress <= steps.get(0).pos) {
			return steps.get(0).color;
		}
		if (progress >= steps.get(steps.size() - 1).pos) {
			return steps.get(steps.size() - 1).color;
		}

		for (int i = 0; i < steps.size() - 1; i++) {
			GradientStep start = steps.get(i);
			GradientStep end = steps.get(i + 1);

			if (progress >= start.pos && progress <= end.pos) {
				float localProgress = (progress - start.pos) / (end.pos - start.pos);
				float eased = end.easing.ease(localProgress, 0, 1, 1);
				return start.color.lerp(eased, end.color);
			}
		}
		return steps.get(steps.size() - 1).color;
	}

	public record GradientStep(Color color, float pos, Easing easing) {
	}
}