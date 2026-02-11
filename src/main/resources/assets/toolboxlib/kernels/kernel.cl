// Helper for random gradients
float2 get_gradient(int2 p) {
    // Standard hash-based gradient generator
    float h = sin((float)p.x * 127.1f + (float)p.y * 311.7f) * 43758.5453123f;
    float2 grad;
    grad.x = sin(h);
    grad.y = cos(h);
    return grad;
}

// Single octave Perlin noise
float perlin(float2 p) {
    int2 i = convert_int2(floor(p));
    float2 f = p - floor(p);

    // Quintic interpolation curve: 6t^5 - 15t^4 + 10t^3
    float2 u = f * f * f * (f * (f * 6.0f - 15.0f) + 10.0f);

    // Dot products of gradients at 4 corners
    float n00 = dot(get_gradient(i + (int2)(0, 0)), f - (float2)(0.0, 0.0));
    float n10 = dot(get_gradient(i + (int2)(1, 0)), f - (float2)(1.0, 0.0));
    float n01 = dot(get_gradient(i + (int2)(0, 1)), f - (float2)(0.0, 1.0));
    float n11 = dot(get_gradient(i + (int2)(1, 1)), f - (float2)(1.0, 1.0));

    // Mix them together
    return mix(mix(n00, n10, u.x),
               mix(n01, n11, u.x), u.y);
}

__kernel void perlinNoise2D(__global float* output,
                            const int width,
                            const int height,
                            const float scale,
                            const int octaves,
                            const float persistence) {
    int x = get_global_id(0);
    int y = get_global_id(1);

    if (x >= width || y >= height) return;

    float2 p = (float2)((float)x / scale, (float)y / scale);
    float total = 0.0f;
    float frequency = 1.0f;
    float amplitude = 1.0f;
    float maxValue = 0.0f;

    for (int i = 0; i < octaves; i++) {
        total += perlin(p * frequency) * amplitude;
        maxValue += amplitude;
        amplitude *= persistence;
        frequency *= 2.0f;
    }

    // Normalizing to [-1, 1] range
    output[y * width + x] = total / maxValue;
}