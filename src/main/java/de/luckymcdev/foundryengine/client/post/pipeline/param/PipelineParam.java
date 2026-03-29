package de.luckymcdev.foundryengine.client.post.pipeline.param;

import de.luckymcdev.foundryengine.client.editor.builtin.PostProcessPanel;
import de.luckymcdev.foundryengine.client.opengl.program.ShaderProgram;
import de.luckymcdev.foundryengine.client.opengl.uniform.Uniform;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * A Parameter / {@link Uniform} for a PostProcess Pipeline, which automatically gets added to the {@link PostProcessPanel}
 * @param <T> The Type for the Value.
 */
public class PipelineParam<T> {

    private final String uniformName;
    private final PipelineParamKind kind;
    private final String displayName;
    private final float min;
    private final float max;
    private final boolean colorPicker;
    private T value;

    /**
     * Creates a new PipelineParam.
     *
     * @param uniformName name of the Uniform to modify.
     * @param displayName Display name in the {@link PostProcessPanel}
     * @param kind        the Kind of Param, check {@link PipelineParamKind} for possible kinds.
     * @param value       the default Value.
     * @param min         minimum value, for Sliders
     * @param max         maximum value, for Sliders.
     * @param colorPicker if it's a ColorPicker
     */
    private PipelineParam(String uniformName, String displayName, PipelineParamKind kind, T value,
                          float min, float max, boolean colorPicker) {
        this.uniformName = uniformName;
        this.displayName = displayName;
        this.kind = kind;
        this.value = value;
        this.min = min;
        this.max = max;
        this.colorPicker = colorPicker;
    }

    /**
     * Param of Type Float.
     */
    public static PipelineParam<Float> floatParam(String uniformName, float value, float min, float max) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.FLOAT, value, min, max, false);
    }

    /**
     * Param of Type Float.
     */
    public static PipelineParam<Float> floatParam(String uniformName, String displayName, float value, float min, float max) {
        return new PipelineParam<>(uniformName, displayName, PipelineParamKind.FLOAT, value, min, max, false);
    }

    /**
     * Param of Type Integer.
     */
    public static PipelineParam<Integer> intParam(String uniformName, int value, int min, int max) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.INT, value, min, max, false);
    }

    /**
     * Param of Type Integer.
     */
    public static PipelineParam<Integer> intParam(String uniformName, String displayName, int value, int min, int max) {
        return new PipelineParam<>(uniformName, displayName, PipelineParamKind.INT, value, min, max, false);
    }

    /**
     * Param of Type Boolean.
     */
    public static PipelineParam<Boolean> boolParam(String uniformName, boolean value) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.BOOLEAN, value, 0, 1, false);
    }

    /**
     * Param of Type Boolean.
     */
    public static PipelineParam<Boolean> boolParam(String uniformName, String displayName, boolean value) {
        return new PipelineParam<>(uniformName, displayName, PipelineParamKind.BOOLEAN, value, 0, 1, false);
    }

    /**
     * Param of Type Vec2f.
     */
    public static PipelineParam<Vector2f> vec2Param(String uniformName, Vector2f value) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.VEC2, value, 0, 1, false);
    }

    /**
     * Param of Type Vec3f.
     */
    public static PipelineParam<Vector3f> vec3Param(String uniformName, Vector3f value) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.VEC3, value, 0, 1, false);
    }

    /**
     * Param of Type Color.
     */
    public static PipelineParam<Vector3f> colorParam(String uniformName, Vector3f value) {
        return new PipelineParam<>(uniformName, uniformName, PipelineParamKind.VEC3, value, 0, 1, true);
    }

    /**
     * Param of Type Color.
     */
    public static PipelineParam<Vector3f> colorParam(String uniformName, String displayName, Vector3f value) {
        return new PipelineParam<>(uniformName, displayName, PipelineParamKind.VEC3, value, 0, 1, true);
    }

    /**
     * Uploads the current value to the given {@link ShaderProgram}
     * as a uniform named {@link #uniformName}.
     */
    public void applyToProgram(ShaderProgram program) {
        program.setUniform(new Uniform<>(uniformName, () -> value));
    }

    public String getUniformName() {
        return uniformName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PipelineParamKind getKind() {
        return kind;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T v) {
        this.value = v;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public boolean isColorPicker() {
        return colorPicker;
    }


}