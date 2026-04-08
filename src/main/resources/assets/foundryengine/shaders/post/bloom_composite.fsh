#version 330

uniform sampler2D InSampler;
uniform sampler2D BloomSampler;

layout (std140) uniform BloomCompositeConfig {
    float Intensity;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 original = texture(InSampler, texCoord);
    vec4 bloom = texture(BloomSampler, texCoord);

    fragColor = vec4(original.rgb + bloom.rgb * Intensity, original.a);
}
