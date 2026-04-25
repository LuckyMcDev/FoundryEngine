package de.luckymcdev.foundryengine.common.bbmodel.data;

public class Element {

    private String name;
    private boolean box_uv;
    private boolean rescale;
    private boolean locked;
    private Integer light_emission;
    private String render_order;
    private boolean allow_mirror_modeling;
    private float[] from;
    private float[] to;
    private Integer autouv;
    private Integer color;
    private float[] origin;
    private Face faces;
    private String type;
    private String uuid;

    public String getName() {
        return name;
    }

    public boolean isBoxUv() {
        return box_uv;
    }

    public boolean isRescale() {
        return rescale;
    }

    public boolean isLocked() {
        return locked;
    }

    public Integer getLightEmission() {
        return light_emission;
    }

    public String getRenderOrder() {
        return render_order;
    }

    public boolean isAllowMirrorModeling() {
        return allow_mirror_modeling;
    }

    public float[] getFrom() {
        return from;
    }

    public float[] getTo() {
        return to;
    }

    public Integer getAutoUv() {
        return autouv;
    }

    public Integer getColor() {
        return color;
    }

    public float[] getOrigin() {
        return origin;
    }

    public Face getFaces() {
        return faces;
    }

    public String getType() {
        return type;
    }

    public String getUuid() {
        return uuid;
    }
}
