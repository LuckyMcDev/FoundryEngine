#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

layout (std140) uniform Intensity {
    float Value;
};

out vec4 fragColor;

void main() {
    fragColor = texture(InSampler, texCoord);
    if (1.0 - (2.0 * abs(texCoord.y - 0.5)) < 0.35 * Value)
    fragColor = vec4(0, 0, 0, 1);
}
