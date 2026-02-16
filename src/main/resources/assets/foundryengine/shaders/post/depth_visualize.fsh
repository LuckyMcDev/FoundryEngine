#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D depthTexture;

void main() {
    float depth = texture(depthTexture, texCoord).r;
    float inverted = 1.0 - depth;

    float visualDepth = pow(inverted, 0.4);

    fragColor = vec4(vec3(visualDepth), 1.0);
}