#version 330

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

layout (std140) uniform DepthVisConfig {
    float Near;
    float Far;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float depth = texture(MainDepthSampler, texCoord).r;
    float linear = Near * Far / (Far - depth * (Far - Near));
    float normalized = log(linear + 1.0) / log(Far + 1.0);

    fragColor = vec4(vec3(normalized), 1.0);
}
