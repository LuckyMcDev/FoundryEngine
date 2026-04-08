#version 330

uniform sampler2D InSampler;

layout (std140) uniform BloomExtractConfig {
    float Threshold;
    float Knee;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);

    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    float rq = clamp(brightness - Threshold + Knee, 0.0, 2.0 * Knee);
    float weight = (rq * rq) / (4.0 * Knee + 0.00001);
    weight = max(weight, brightness - Threshold) / max(brightness, 0.00001);

    fragColor = vec4(color.rgb * weight, color.a);
}
