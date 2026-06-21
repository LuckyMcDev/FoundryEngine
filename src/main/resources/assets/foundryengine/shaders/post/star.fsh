#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

layout (std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout (std140) uniform Intensity {
    float Value;
};

out vec4 fragColor;


float sdPentagram(in vec2 p, in float r)
{
    const float k1x = 0.809016994;
    const float k2x = 0.309016994;
    const float k1y = 0.587785252;
    const float k2y = 0.951056516;
    const float k1z = 0.726542528;
    const vec2 v1 = vec2(k1x, -k1y);
    const vec2 v2 = vec2(-k1x, -k1y);
    const vec2 v3 = vec2(k2x, -k2y);

    p.x = abs(p.x);
    p -= 2.0 * max(dot(v1, p), 0.0) * v1;
    p -= 2.0 * max(dot(v2, p), 0.0) * v2;
    p.x = abs(p.x);
    p.y -= r;
    return length(p - v3 * clamp(dot(p, v3), 0.0, k1z * r))
    * sign(p.y * v3.x - p.x * v3.y);
}

void main() {
    vec4 diffuseColor = texture(InSampler, texCoord);
    vec2 fragCoord = texCoord * OutSize;

    vec2 screenCenter = (2.0 * fragCoord - OutSize) / OutSize.y;
    float radius = 5.2 * (1.0 - (1 - pow(1 - Value, 3)));

    if (sdPentagram(screenCenter, radius) > 0.0)
    diffuseColor = vec4(0.0, 0.0, 0.0, 1.0);

    fragColor = diffuseColor;
}
