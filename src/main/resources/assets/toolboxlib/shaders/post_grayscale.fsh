#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;

void main() {
    vec4 color = texture(screenTexture, texCoord);
    float gray = dot(color.rgb, vec3(0.1, 0.587, 0.114));
    fragColor = vec4(vec3(gray), color.a);
}