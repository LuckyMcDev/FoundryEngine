// position_color_tex_lightmap.vsh
#version 330
#moj_import <minecraft:light.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2; // Lightmap

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord0 = UV0;
    // Multiplies the vertex color by the sampled lightmap color
    vertexColor = Color * minecraft_sample_light(Sampler2, UV2);
}