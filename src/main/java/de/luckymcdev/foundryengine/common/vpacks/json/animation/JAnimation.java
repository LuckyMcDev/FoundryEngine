package de.luckymcdev.foundryengine.common.vpacks.json.animation;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JAnimation {
    private Boolean interpolate;
    private Integer width;
    private Integer height;
    @SerializedName("frametime")
    private Integer frameTime;
    private List<JFrame> frames;
    private List<Integer> defaultFrames;

    public JAnimation interpolate() {
        this.interpolate = true;
        return this;
    }

    public JAnimation width(int width) {
        this.width = width;
        return this;
    }

    public JAnimation height(int height) {
        this.height = height;
        return this;
    }

    public JAnimation frameTime(int time) {
        this.frameTime = time;
        return this;
    }

    public JAnimation add(int frame) {
        if (this.defaultFrames == null) {
            this.defaultFrames = new ArrayList<>();
        }
        this.defaultFrames.add(frame);
        return this;
    }

    public JAnimation add(JFrame frame) {
        if (this.frames == null) {
            this.frames = new ArrayList<>();
        }
        this.frames.add(frame);
        return this;
    }

    public static class Serializer implements JsonSerializer<JAnimation> {
        @Override
        public JsonElement serialize(JAnimation src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            JsonObject animationJson = getAnimationJson(src);

            JsonArray frames = new JsonArray();
            if (src.frames != null) {
                for (JFrame frame : src.frames) {
                    frames.add(context.serialize(frame));
                }
            }
            if (src.defaultFrames != null) {
                for (Integer defaultFrame : src.defaultFrames) {
                    frames.add(defaultFrame);
                }
            }
            if (!frames.isEmpty()) {
                animationJson.add("frames", frames);
            }

            jsonObject.add("animation", animationJson);
            return jsonObject;
        }

        private @NotNull JsonObject getAnimationJson(JAnimation src) {
            JsonObject animationJson = new JsonObject();
            if (src.interpolate != null) {
                animationJson.addProperty("interpolate", src.interpolate);
            }
            if (src.width != null) {
                animationJson.addProperty("width", src.width);
            }
            if (src.height != null) {
                animationJson.addProperty("height", src.height);
            }
            if (src.frameTime != null) {
                animationJson.addProperty("frametime", src.frameTime);
            }
            return animationJson;
        }
    }
}
