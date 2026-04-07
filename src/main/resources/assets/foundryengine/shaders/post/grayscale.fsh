#version 330

uniform sampler2D InSampler;
in vec2 texCoord;

out vec4 fragColor;

// Standard Grayscale weights for Red, Green, and Blue
const vec3 GrayWeights = vec3(0.3, 0.59, 0.11);

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    // Calculate the brightness (Luma)
    float luma = dot(InTexel.rgb, GrayWeights);

    // Output the luma value across all three color channels
    fragColor = vec4(vec3(luma), InTexel.a);
}