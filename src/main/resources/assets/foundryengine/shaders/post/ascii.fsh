#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform sampler2D depthTexture;
uniform vec2 resolution;

// --- SETTINGS ---
// Increase this if the screen is too dark (e.g. 1.5)
// Decrease if it's too washed out (e.g. 0.8)
const float BRIGHTNESS_MULTIPLIER = 1.0;

// Higher values = more contrast (spreads out the characters)
const float CONTRAST_POWER = 1.2;

// --- CHARACTER BITMAPS (5x5) ---
// We define a smoother gradient from dark to light
const int C_BLANK = 0;
const int C_DOT   = 4096;      // .
const int C_COMMA = 131200;    // ,
const int C_MINUS = 31744;     // -
const int C_TILDE = 14812352;  // ~
const int C_PLUS  = 163153;    // +
const int C_EQUAL = 12603659;  // =
const int C_STAR  = 2750033;   // *
const int C_HASH  = 11512810;  // #
const int C_AT    = 13195790;  // @

// Edge characters (for outlines)
const int C_EDGE_V = 4329604;  // |
const int C_EDGE_H = 31744;    // -
const int C_EDGE_D1 = 17043521;// /
const int C_EDGE_D2 = 1118480; // \

float getBit(int n, vec2 p) {
    p = floor(p * vec2(4.0, -4.0) + 2.5);
    if (clamp(p.x, 0.0, 4.0) == p.x && clamp(p.y, 0.0, 4.0) == p.y) {
        int a = int(p.x) + 5 * int(p.y);
        if (((n >> a) & 1) == 1) return 1.0;
    }
    return 0.0;
}

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

// Simple depth sampler (non-linear is usually fine for edge detection)
float getDepth(vec2 coord) {
    return texture(depthTexture, coord).r;
}

void main() {
    vec2 pix = floor(texCoord * resolution) + 0.5;
    vec2 blockPos = floor(pix / 8.0);
    vec2 charPos = mod(pix, 8.0);
    vec2 p = (charPos / 4.0) - vec2(1.0);
    vec2 centerUV = (blockPos * 8.0 + vec2(4.0)) / resolution;

    vec3 col = texture(screenTexture, centerUV).rgb;
    float lum = luminance(col);

    lum = pow(lum * BRIGHTNESS_MULTIPLIER, CONTRAST_POWER);

    vec2 texel = 1.0 / resolution;
    float d = getDepth(centerUV);
    float dN = getDepth(centerUV + vec2(0, texel.y));
    float dE = getDepth(centerUV + vec2(texel.x, 0));

    float edgeDiff = abs(d - dN) + abs(d - dE);
    bool isEdge = edgeDiff > 0.02;

    int charID = C_BLANK;

    if (isEdge) {
        // Simple edge logic (expand if you want directional edges back)
        charID = C_EDGE_V;
        // If edge is horizontal, use - (optional check)
        if (abs(d - dN) > abs(d - dE)) charID = C_EDGE_H;
    }
    else {
        // Smooth Luminance Ramp
        if (lum > 0.10) charID = C_DOT;
        if (lum > 0.25) charID = C_COMMA;
        if (lum > 0.40) charID = C_MINUS; // Mid-tones get softer '-'
        if (lum > 0.55) charID = C_TILDE; // instead of harsh '+'
        if (lum > 0.70) charID = C_EQUAL;
        if (lum > 0.85) charID = C_HASH;
        if (lum > 0.95) charID = C_AT;
    }

    float mask = getBit(charID, p);

    // Soft blend: Keep 10% of original color in background for readability
    vec3 finalCol = mix(col * 0.1, col * 1.2, mask);

    fragColor = vec4(finalCol, 1.0);
}