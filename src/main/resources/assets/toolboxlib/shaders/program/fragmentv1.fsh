#version 330 core

uniform sampler2D DiffuseSampler;

in vec2 vUv;
out vec4 FragColor;

void main() {
    vec4 color = texture(DiffuseSampler, vUv);
    vec4 tint = vec4(1.0, 0.0, 0.0, 1.0);
    FragColor = mix(color, tint, 0.25);
}
