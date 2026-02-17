#version 330 core

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform sampler2D depthTexture;
uniform vec2 resolution;
uniform float cellSize;

// --- SETTINGS ---
const float BRIGHTNESS_MULTIPLIER = 1.0;
const float CONTRAST_POWER = 1.2;

// --- CHARACTER BITMAPS (5x5) ---
const int C_BLANK = 0;
const int C_DOT   = 4096;
const int C_COMMA = 131200;
const int C_MINUS = 31744;
const int C_TILDE = 14812352;
const int C_PLUS  = 163153;
const int C_EQUAL = 12603659;
const int C_STAR  = 2750033;
const int C_HASH  = 11512810;
const int C_AT    = 13195790;

const int C_EDGE_V = 4329604;
const int C_EDGE_H = 31744;

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

float getDepth(vec2 coord) {
    return texture(depthTexture, coord).r;
}

void main() {
    vec2 pix = floor(texCoord * resolution) + 0.5;

    vec2 blockPos = floor(pix / cellSize);
    vec2 charPos = mod(pix, cellSize);

    vec2 p = (charPos / (cellSize * 0.5)) - vec2(1.0);

    vec2 centerUV = (blockPos * cellSize + vec2(cellSize * 0.5)) / resolution;

    vec2 texel = 1.0 / resolution;
    float d = getDepth(centerUV);
    float dN = getDepth(centerUV + vec2(0, texel.y));
    float dE = getDepth(centerUV + vec2(texel.x, 0));

    vec3 col = texture(screenTexture, centerUV).rgb;
    float lum = luminance(col);

    float depthLum = pow(1.0 - d, 0.35);
    float baseLum = max(lum, depthLum * 0.9);
    baseLum = max(baseLum, 0.12);
    float lumForChars = pow(baseLum * BRIGHTNESS_MULTIPLIER, CONTRAST_POWER);
    vec3 baseCol = mix(col, vec3(baseLum), 0.6);

    float edgeDiff = abs(d - dN) + abs(d - dE);
    bool isEdge = edgeDiff > 0.02;

    int charID = C_BLANK;

    if (isEdge) {
        charID = (abs(d - dN) > abs(d - dE)) ? C_EDGE_H : C_EDGE_V;
    } else {
        if (lumForChars > 0.10) charID = C_DOT;
        if (lumForChars > 0.25) charID = C_COMMA;
        if (lumForChars > 0.40) charID = C_MINUS;
        if (lumForChars > 0.55) charID = C_TILDE;
        if (lumForChars > 0.70) charID = C_EQUAL;
        if (lumForChars > 0.85) charID = C_HASH;
        if (lumForChars > 0.95) charID = C_AT;
    }

    float mask = getBit(charID, p);
    vec3 finalCol = mix(baseCol * 0.2, baseCol * 1.2, mask);

    fragColor = vec4(finalCol, 1.0);
}