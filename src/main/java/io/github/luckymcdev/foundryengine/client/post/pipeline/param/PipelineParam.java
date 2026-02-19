package io.github.luckymcdev.foundryengine.client.post.pipeline.param;

import io.github.luckymcdev.foundryengine.client.opengl.shaders.program.ShaderProgram;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import  org.joml.*;

/**
 * A named, typed, mutable parameter belonging to a {@link io.github.luckymcdev.foundryengine.client.post.pipeline.PostProcessPipeline}.
 *
 * <p>Pipelines register params in their constructor via
 * {@code addParam(new PipelineParam<>(...))}. The {@code PostProcessManager} then
 * calls {@link #applyToProgram} on every param each frame, and the
 * {@code PostProcessPanel} reflects over the params to render ImGui widgets.</p>
 *
 * <h3>Supported value types</h3>
 * <ul>
 *   <li>{@code Float}   – rendered as a drag/slider float</li>
 *   <li>{@code Integer} – rendered as a drag/slider int</li>
 *   <li>{@code Boolean} – rendered as a checkbox</li>
 *   <li>{@code  Vector2f} – rendered as two floats</li>
 *   <li>{@code  Vector3f} – rendered as three floats (color picker or xyz)</li>
 * </ul>
 *
 * @param <T> the value type
 */
public class PipelineParam<T> {

    public enum Kind { FLOAT, INT, BOOLEAN, VEC2, VEC3 }

    private final String uniformName;
    private final Kind   kind;
    private       T      value;

    // Optional display hints (used by the ImGui panel)
    private final String displayName;
    private final float  min;
    private final float  max;
    /** When true the panel renders a color picker instead of xyz sliders for VEC3. */
    private final boolean colorPicker;

    // ── Factories ─────────────────────────────────────────────────────────────

    public static PipelineParam<Float> floatParam(String uniformName, float value, float min, float max) {
        return new PipelineParam<>(uniformName, uniformName, Kind.FLOAT, value, min, max, false);
    }

    public static PipelineParam<Float> floatParam(String uniformName, String displayName, float value, float min, float max) {
        return new PipelineParam<>(uniformName, displayName, Kind.FLOAT, value, min, max, false);
    }

    public static PipelineParam<Integer> intParam(String uniformName, int value, int min, int max) {
        return new PipelineParam<>(uniformName, uniformName, Kind.INT, value, min, max, false);
    }

    public static PipelineParam<Integer> intParam(String uniformName, String displayName, int value, int min, int max) {
        return new PipelineParam<>(uniformName, displayName, Kind.INT, value, min, max, false);
    }

    public static PipelineParam<Boolean> boolParam(String uniformName, boolean value) {
        return new PipelineParam<>(uniformName, uniformName, Kind.BOOLEAN, value, 0, 1, false);
    }

    public static PipelineParam<Boolean> boolParam(String uniformName, String displayName, boolean value) {
        return new PipelineParam<>(uniformName, displayName, Kind.BOOLEAN, value, 0, 1, false);
    }

    public static PipelineParam<Vector2f> vec2Param(String uniformName, Vector2f value) {
        return new PipelineParam<>(uniformName, uniformName, Kind.VEC2, value, 0, 1, false);
    }

    public static PipelineParam<Vector3f> vec3Param(String uniformName, Vector3f value) {
        return new PipelineParam<>(uniformName, uniformName, Kind.VEC3, value, 0, 1, false);
    }

    public static PipelineParam<Vector3f> colorParam(String uniformName, Vector3f value) {
        return new PipelineParam<>(uniformName, uniformName, Kind.VEC3, value, 0, 1, true);
    }

    public static PipelineParam<Vector3f> colorParam(String uniformName, String displayName, Vector3f value) {
        return new PipelineParam<>(uniformName, displayName, Kind.VEC3, value, 0, 1, true);
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    private PipelineParam(String uniformName, String displayName, Kind kind, T value,
                          float min, float max, boolean colorPicker) {
        this.uniformName = uniformName;
        this.displayName = displayName;
        this.kind        = kind;
        this.value       = value;
        this.min         = min;
        this.max         = max;
        this.colorPicker = colorPicker;
    }

    // ── Apply to shader ───────────────────────────────────────────────────────

    /**
     * Uploads the current value to the given {@link ShaderProgram}
     * as a uniform named {@link #uniformName}.
     */
    public void applyToProgram(ShaderProgram program) {
        program.setUniform(new Uniform<>(uniformName, value));
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String  getUniformName() { return uniformName; }
    public String  getDisplayName() { return displayName; }
    public Kind    getKind()        { return kind; }
    public T       getValue()       { return value; }
    public void    setValue(T v)    { this.value = v; }
    public float   getMin()         { return min; }
    public float   getMax()         { return max; }
    public boolean isColorPicker()  { return colorPicker; }
}