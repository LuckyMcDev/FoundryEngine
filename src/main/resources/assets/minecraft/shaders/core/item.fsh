#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:skybox.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 vertexPosition;

out vec4 fragColor;

bool isTextureAlpha(float valueToExpected) {
    float epsilon = 1.0;
    float colorValue = texture(Sampler0, texCoord0).a * 255.0;
    return abs(colorValue - valueToExpected) < epsilon;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    if (isTextureAlpha(254.0)) {
        vec3 viewDir = normalize(vertexPosition);
        vec3 clouds = extractSky(viewDir);
        fragColor = vec4(clouds, 1.0);
        return;
    }

    #ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
    #endif

    color *= vertexColor * ColorModulator;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}