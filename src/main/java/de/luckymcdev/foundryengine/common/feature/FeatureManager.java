package de.luckymcdev.foundryengine.common.feature;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class FeatureManager {
    private final List<EngineFeature> features = new ArrayList<>();

    public void register(EngineFeature feature) {
        this.features.add(feature);
    }

    public void remove(EngineFeature feature) {
        this.features.remove(feature);
    }

    public boolean isEnabled(Identifier identifier) {
        return get(identifier).isEnabled();
    }

    public boolean isEnabled(EngineFeature feature) {
        return get(feature.identifier()).isEnabled();
    }

    public EngineFeature get(Identifier identifier) {
        return this.features.stream()
                .filter(feature -> feature.identifier().equals(identifier))
                .findFirst()
                .orElse(null);
    }

    public List<EngineFeature> getFeatures() {
        return features;
    }
}
