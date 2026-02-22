#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;

void main() {
    vec4 color = texture(screenTexture, texCoord);
    fragColor = vec4(1.0 - color.rgb, color.a);
}