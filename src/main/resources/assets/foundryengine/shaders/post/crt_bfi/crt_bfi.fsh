#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform vec2 resolution;
uniform int frameCount;

uniform float framesPerHz;
uniform float gain;

const float GAMMA = 2.4;

// Decode sRGB to Linear for math
vec3 srgb2linear(vec3 c) {
    return pow(c, vec3(GAMMA));
}

// Encode Linear back to sRGB for display
vec3 linear2srgb(vec3 c) {
    return pow(c, vec3(1.0 / GAMMA));
}

void main() {
    // 1. Calculate the current position of the "Electron Beam"
    // This moves vertically based on the frameCount
    float crtRasterPos = mod(float(frameCount), framesPerHz) / framesPerHz;

    // 2. Sample and linearize the color
    vec3 rawColor = texture(screenTexture, texCoord).rgb;
    vec3 colorLinear = srgb2linear(rawColor);

    // Scale brightness based on the framesPerHz to simulate phosphor intensity
    vec3 colorScaled = colorLinear * framesPerHz * gain;

    // 3. Rolling Scan Logic (Top to Bottom)
    float tubePos = (1.0 - texCoord.y);
    float tubeFrame = tubePos * framesPerHz;
    float beamStart = crtRasterPos * framesPerHz;
    float beamEnd = beamStart + 1.0;

    vec3 result = vec3(0.0);

    // Calculate how much the "beam" overlaps this specific pixel's vertical position
    for (int i = 0; i < 3; i++) {
        float L = colorScaled[i];
        float overlap = max(0.0, min(tubeFrame + L, beamEnd) - max(tubeFrame, beamStart));
        result[i] = overlap;
    }

    // 4. Final Output
    fragColor = vec4(linear2srgb(result), 1.0);
}