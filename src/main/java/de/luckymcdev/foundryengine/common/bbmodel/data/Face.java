package de.luckymcdev.foundryengine.common.bbmodel.data;

public class Face {

    private FaceValue north;
    private FaceValue east;
    private FaceValue south;
    private FaceValue west;
    private FaceValue up;
    private FaceValue down;

    public FaceValue getNorth() {
        return north;
    }

    public FaceValue getEast() {
        return east;
    }

    public FaceValue getSouth() {
        return south;
    }

    public FaceValue getWest() {
        return west;
    }

    public FaceValue getUp() {
        return up;
    }

    public FaceValue getDown() {
        return down;
    }

    public static class FaceValue {
        private float[] uv;
        private Integer texture;
        private Integer rotation;
        private String cullface;

        public float[] getUv() {
            return uv;
        }

        public Integer getTexture() {
            return texture;
        }

        public Integer getRotation() {
            return rotation;
        }

        public String getCullface() {
            return cullface;
        }
    }

}
