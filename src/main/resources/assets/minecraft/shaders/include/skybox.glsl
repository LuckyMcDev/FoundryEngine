#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:skybox_time.glsl>

const float skyScale = 1.2;
const float timeSpeed = 0.03;

// Adjusted Palettes for balanced brightness profiles
// 1. NIGHT PALETTE (Deep cosmic void, space nebulas, and stars)
const vec3 spaceColorBgNight     = vec3(0.005, 0.005, 0.015);
const vec3 nebulaColorNight1     = vec3(0.5, 0.08, 0.45);   // Cosmic Purple
const vec3 nebulaColorNight2     = vec3(0.02, 0.35, 0.5);   // Deep Electric Teal
const vec3 horizonGlowColorNight = vec3(0.2, 0.05, 0.4);

// 2. SUNSET PALETTE (Vibrant, warm, rich tones)
const vec3 spaceColorBgSunset     = vec3(0.12, 0.03, 0.08);
const vec3 nebulaColorSunset1     = vec3(0.85, 0.18, 0.05);  // Deep Fiery Orange
const vec3 nebulaColorSunset2     = vec3(0.8, 0.5, 0.05);    // Rich Golden Amber
const vec3 horizonGlowColorSunset = vec3(0.9, 0.3, 0.05);

// 3. DAY PALETTE (Soft, stylized atmospheric daylight)
const vec3 spaceColorBgDay       = vec3(0.08, 0.22, 0.45);  // Deep sky blue base
const vec3 nebulaColorDay1       = vec3(0.3, 0.55, 0.8);   // Soft atmospheric whisps
const vec3 nebulaColorDay2       = vec3(0.55, 0.75, 0.95);  // Daylight cirrus
const vec3 horizonGlowColorDay   = vec3(0.4, 0.6, 0.85);

const mat2 rotationMat = mat2(0.8, 0.6, -0.6, 0.8);

vec2 spaceHash(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}

float cellularNoise(vec2 p, float customTime) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float minDist = 1.0;

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 point = spaceHash(i + neighbor);
            point = 0.5 + 0.5 * sin(customTime * 4.0 + point * 6.2831);
            vec2 diff = neighbor + point - f;
            minDist = min(minDist, length(diff));
        }
    }
    return minDist;
}

float nebulaFbm(vec2 p, float customTime) {
    float value = 0.0;
    float amplitude = 0.55;
    for (int i = 0; i < 5; i++) {
        float noise = cellularNoise(p, customTime);
        value += amplitude * (1.0 - noise * noise);
        p = rotationMat * p * 2.1;
        amplitude *= 0.48;
    }
    return value;
}

vec3 extractSky(vec3 rayDir) {
    // Multi-matrix synthetic clock fallback loop to ensure motion
    float syntheticTime = GameTime + (ModelViewMat[3][0] * 0.005) + (ModelViewMat[3][2] * 0.005);
    float t = syntheticTime * timeSpeed;

    // Retrieve global time metrics safely
    SkyTimeInfo time = getSkyTime();

    // 3-Way linear color interpolation
    vec3 spaceColorBg = mix(mix(spaceColorBgNight, spaceColorBgSunset, time.sunsetFactor), spaceColorBgDay, time.dayFactor);
    vec3 nebulaColor1 = mix(mix(nebulaColorNight1, nebulaColorSunset1, time.sunsetFactor), nebulaColorDay1, time.dayFactor);
    vec3 nebulaColor2 = mix(mix(nebulaColorNight2, nebulaColorSunset2, time.sunsetFactor), nebulaColorDay2, time.dayFactor);
    vec3 horizonGlowColor = mix(mix(horizonGlowColorNight, horizonGlowColorSunset, time.sunsetFactor), horizonGlowColorDay, time.dayFactor);

    // Coordinate projection
    vec2 uv = rayDir.xz / (abs(rayDir.y) + 0.12);
    uv *= skyScale;

    vec2 uv1 = uv + vec2(t * 0.3, t * 0.15);
    vec2 uv2 = rotationMat * uv - vec2(t * 0.1, -t * 0.2);

    float layer1 = nebulaFbm(uv1, syntheticTime);
    vec2 warpUV = uv2 + vec2(cos(layer1 * 4.0), sin(layer1 * 4.0)) * 0.4;
    float layer2 = nebulaFbm(warpUV, syntheticTime);

    float verticalGradient = clamp(abs(rayDir.y) * 0.5 + 0.5, 0.0, 1.0);
    vec3 baseSky = mix(spaceColorBg, spaceColorBg * 0.25, verticalGradient);

    vec3 nebColor = mix(nebulaColor1, nebulaColor2, layer1);
    vec3 finalNebula = nebColor * (pow(layer2, 1.5) * 1.6);

    // Stars visible only during nighttime (where dayFactor & sunsetFactor reach zero)
    float starPatternA = cellularNoise(uv * 14.0 + vec2(t * 0.02), syntheticTime);
    float starsA = smoothstep(0.015, 0.0, starPatternA) * (0.3 + 0.7 * sin(syntheticTime * 0.4 + starPatternA * 10.0));

    float starPatternB = cellularNoise(uv * 6.0 - vec2(t * 0.01), syntheticTime);
    float starsB = smoothstep(0.025, 0.0, starPatternB) * (0.4 + 0.6 * cos(syntheticTime * 0.3 + starPatternB * 20.0));

    float starVisibility = clamp(1.0 - (time.dayFactor + time.sunsetFactor), 0.0, 1.0);
    vec3 starField = vec3(starsA * 0.5 + starsB * 1.0) * starVisibility;

    // Horizon blend
    float horizonGlowWeight = pow(1.0 - clamp(abs(rayDir.y), 0.0, 1.0), 4.5);
    vec3 horizonGlow = horizonGlowColor * horizonGlowWeight * (0.3 + 0.2 * layer1);

    vec3 result = baseSky + finalNebula + starField + horizonGlow;

    // Horizon ambient drop-off mask
    float horizonMask = smoothstep(0.0, 0.25, abs(rayDir.y));
    result = mix(spaceColorBg, result, horizonMask);

    return clamp(result, 0.0, 1.0);
}