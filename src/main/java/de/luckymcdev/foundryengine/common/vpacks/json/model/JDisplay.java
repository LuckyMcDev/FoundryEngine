package de.luckymcdev.foundryengine.common.vpacks.json.model;

import com.google.gson.annotations.SerializedName;

public class JDisplay implements Cloneable {
    @SerializedName("thirdperson_righthand")
    private JPosition thirdPersonRightHand;
    @SerializedName("thirdperson_lefthand")
    private JPosition thirdPersonLeftHand;
    @SerializedName("firstperson_righthand")
    private JPosition firstPersonRightHand;
    @SerializedName("firstperson_lefthand")
    private JPosition firstPersonLeftHand;
    private JPosition gui;
    private JPosition head;
    private JPosition ground;
    private JPosition fixed;

    public JDisplay thirdPersonRightHand(JPosition thirdPersonRightHand) {
        this.thirdPersonRightHand = thirdPersonRightHand;
        return this;
    }

    public JDisplay thirdPersonLeftHand(JPosition thirdPersonLeftHand) {
        this.thirdPersonLeftHand = thirdPersonLeftHand;
        return this;
    }

    public JDisplay firstPersonRightHand(JPosition firstPersonRightHand) {
        this.firstPersonRightHand = firstPersonRightHand;
        return this;
    }

    public JDisplay firstPersonLeftHand(JPosition firstPersonLeftHand) {
        this.firstPersonLeftHand = firstPersonLeftHand;
        return this;
    }

    public JDisplay gui(JPosition gui) {
        this.gui = gui;
        return this;
    }

    public JDisplay head(JPosition head) {
        this.head = head;
        return this;
    }

    public JDisplay ground(JPosition ground) {
        this.ground = ground;
        return this;
    }

    public JDisplay fixed(JPosition fixed) {
        this.fixed = fixed;
        return this;
    }

    @Override
    public JDisplay clone() {
        try {
            return (JDisplay) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
