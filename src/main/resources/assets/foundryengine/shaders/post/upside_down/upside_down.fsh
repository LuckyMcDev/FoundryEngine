#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;

void main() {
    vec2 flippedCoord = vec2(texCoord.x, 1.0 - texCoord.y);
    fragColor = texture(screenTexture, flippedCoord);
}