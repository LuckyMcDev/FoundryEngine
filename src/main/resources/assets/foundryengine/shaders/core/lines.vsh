// lines.vsh
#version 330
#moj_import < minecraft: dynamictransforms.glsl >
#moj_import <minecraft: projection.glsl >

in vec3 Position;
in vec4 Color;
in vec3 Normal; // Used to help offset lines for thickness
out vec4 vertexColor;

void main() {
gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
vertexColor = Color;
}