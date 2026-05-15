//position_color.fsh
#version 330
#moj_import < minecraft: dynamictransforms.glsl >

in vec4 vertexColor;
out vec4 fragColor;

void main() {
fragColor = vertexColor * ColorModulator;
if (fragColor.a < 0.1) discard;
}