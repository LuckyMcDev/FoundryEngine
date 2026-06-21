#version 330

#moj_import <minecraft:fog.glsl>

struct SkyTimeInfo {
    float dayFactor;    // 1.0 = Pure Daylight, 0.0 = Pure Night
    float sunsetFactor; // 1.0 = Peak Sunset/Sunrise, 0.0 = Else
};

SkyTimeInfo getSkyTime() {
    // Standard color perception weights to check overall sky illumination
    float luminance = dot(FogColor.rgb, vec3(0.2126, 0.7152, 0.0722));

    // Smooth, wide curves to capture transitions across all dimensions
    // At night, luminance is extremely low (< 0.05). In full daylight it is high (> 0.4)
    float day = smoothstep(0.04, 0.35, luminance);

    // Sunset occurs specifically when daylight is fading but color balance shifts.
    // We isolate this by measuring how close the current light is to the "tipping point"
    float fadeTransition = smoothstep(0.0, 0.6, day) * smoothstep(1.0, 0.3, day);

    // We enhance it by reading the underlying red-to-green variance
    // Minecraft's sunset fog has higher Red/Green saturation than standard blue daylight
    float colorShift = max(0.0, FogColor.r - FogColor.b * 0.8) * 2.0;

    SkyTimeInfo time;
    time.sunsetFactor = clamp(fadeTransition * (0.3 + colorShift), 0.0, 1.0);
    time.dayFactor = clamp(day - time.sunsetFactor * 0.7, 0.0, 1.0);

    return time;
}