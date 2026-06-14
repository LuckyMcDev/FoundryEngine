#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec3 vPos;
in vec2 vUV;
in vec4 vColor;
in vec3 vNormal;

out vec4 fragColor;

void main() {
    vec4 color = vColor * ColorModulator;
    if (color.a < 0.1) discard;
    fragColor = color;
}
