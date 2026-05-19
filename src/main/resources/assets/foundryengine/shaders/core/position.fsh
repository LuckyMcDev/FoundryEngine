#version 330
#moj_import <minecraft:dynamictransforms.glsl>

out vec4 fragColor;

void main() {
    // Since there is no vertex color, we rely purely on the global color modulator
    fragColor = ColorModulator;
}