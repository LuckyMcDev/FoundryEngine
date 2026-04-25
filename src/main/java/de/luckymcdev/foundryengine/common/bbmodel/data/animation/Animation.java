package de.luckymcdev.foundryengine.common.bbmodel.data.animation;

import java.util.Map;

public class Animation {

    private String uuid;
    private String name;
    private String loop;
    private boolean override;
    private Float length;
    private int snapping;
    private boolean selected;
    private String anim_time_update;
    private String blend_weight;
    private String start_delay;
    private String loop_delay;
    private Map<String, Animator> animators;

    private String animation_variable_placeholders;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getLoop() {
        return loop;
    }

    public boolean getOverride() {
        return override;
    }

    public Float getLength() {
        return length;
    }

    public int getSnapping() {
        return snapping;
    }

    public boolean getSelected() {
        return selected;
    }

    public String getAnimTimeUpdate() {
        return anim_time_update;
    }

    public String getBlendWeight() {
        return blend_weight;
    }

    public String getStartDelay() {
        return start_delay;
    }

    public String getLoopDelay() {
        return loop_delay;
    }

    public Map<String, Animator> getAnimators() {
        return animators;
    }

    public String getAnimationVariablePlaceholders() {
        return animation_variable_placeholders;
    }

}
