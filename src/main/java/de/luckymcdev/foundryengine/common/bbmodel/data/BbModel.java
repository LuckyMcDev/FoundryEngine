package de.luckymcdev.foundryengine.common.bbmodel.data;

import de.luckymcdev.foundryengine.common.bbmodel.data.animation.Animation;

public class BbModel {

    private Meta meta;

    private String name;
    private String model_identifier;
    private float[] visible_box;
    private String variable_placeholders;
    private Object[] variable_placeholder_buttons;
    private Object[] timeline_setups;
    private Object unhandled_root_fields;
    private Object[] overrides;

    private Resolution resolution;

    private Element[] elements;
    private Outliner[] outliner;
    private Texture[] textures;
    private Animation[] animations;

    public Meta getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getModelIdentifier() {
        return model_identifier;
    }

    public float[] getVisibleBox() {
        return visible_box;
    }

    public String getVariablePlaceholders() {
        return variable_placeholders;
    }

    public Object[] getVariablePlaceholderButtons() {
        return variable_placeholder_buttons;
    }

    public Object[] getTimelineSetups() {
        return timeline_setups;
    }

    public Object getUnhandledRootFields() {
        return unhandled_root_fields;
    }

    public Object[] getOverrides() {
        return overrides;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public Element[] getElements() {
        return elements;
    }

    public Outliner[] getOutliner() {
        return outliner;
    }

    public Texture[] getTextures() {
        return textures;
    }

    public Animation[] getAnimations() {
        return animations;
    }

}
