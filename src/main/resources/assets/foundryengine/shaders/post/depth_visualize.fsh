#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D depthTexture;

void main() {
    // 1. Sample raw depth
    float depth = texture(depthTexture, texCoord).r;

    // 2. Do NOT invert if you want standard depth visualization
    // where the sky is white and close objects are dark.
    float visualDepth = pow(depth, 0.4);

    fragColor = vec4(vec3(visualDepth), 1.0);
}