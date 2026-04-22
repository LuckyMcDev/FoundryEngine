#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

layout (std140) uniform ProgressBuffer {
    float Progress;
};

out vec4 fragColor;

void main() {
    vec4 diffuseColor = texture(InSampler, texCoord);

    vec4 outColor = mix(diffuseColor, vec4(0.0, 0.0, 0.0, 1.0), Progress);
    fragColor = vec4(outColor.rgb, 1.0);
}