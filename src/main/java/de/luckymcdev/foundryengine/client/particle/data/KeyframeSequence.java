package de.luckymcdev.foundryengine.client.particle.data;

import de.luckymcdev.foundryengine.common.easing.Easing;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeyframeSequence<T> {
    private final List<Keyframe<T>> keyframes = new ArrayList<>();

    public KeyframeSequence<T> add(T value, float timepoint, Easing easing) {
        keyframes.add(new Keyframe<>(value, timepoint, easing));
        keyframes.sort(Comparator.comparingDouble(k -> k.timepoint));
        return this;
    }

    public @Nullable InterpolationContext<T> getInterpolation(float progress) {
        if (keyframes.isEmpty()) return null;

        if (progress <= keyframes.getFirst().timepoint)
            return new InterpolationContext<>(keyframes.getFirst().value, keyframes.getFirst().value, 0f, Easing.LINEAR);

        if (progress >= keyframes.getLast().timepoint)
            return new InterpolationContext<>(keyframes.getLast().value, keyframes.getLast().value, 1f, Easing.LINEAR);

        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe<T> start = keyframes.get(i);
            Keyframe<T> end = keyframes.get(i + 1);

            if (progress >= start.timepoint && progress <= end.timepoint) {
                float localProgress = (progress - start.timepoint) / (end.timepoint - start.timepoint);
                return new InterpolationContext<>(start.value, end.value, localProgress, end.easing);
            }
        }
        return null;
    }

    public @Nullable T getFirstValue() {
        return keyframes.isEmpty() ? null : keyframes.getFirst().value();
    }

    public record Keyframe<T>(T value, float timepoint, Easing easing) {
    }

    public record InterpolationContext<T>(T start, T end, float localProgress, Easing easing) {
        public float eased() {
            return easing.ease(localProgress, 0, 1, 1);
        }
    }
}