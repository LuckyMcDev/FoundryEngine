#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

layout (std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout (std140) uniform ProgressBuffer {
    float Progress;
};

out vec4 fragColor;

void main() {
    vec4 diffuseColor = texture(InSampler, texCoord);
    vec2 fragCoord = texCoord * OutSize;

    vec2 screenCenter = OutSize * 0.5;
    float radius = sqrt(pow(OutSize.y * 0.5, 2.0) + pow(OutSize.x * 0.5, 2.0)) * (1.0 - Progress);

    if (sqrt(pow(fragCoord.x - screenCenter.x, 2.0) + pow(fragCoord.y - screenCenter.y, 2.0)) > radius)
    diffuseColor = vec4(0.0, 0.0, 0.0, 1.0);

    fragColor = diffuseColor;
}