#version 330 core

in vec2 fragPos;
out vec4 FragColor;

void main() {
    // Create a nice gradient based on position
    vec2 uv = fragPos * 0.5 + 0.5; // Convert from [-1,1] to [0,1]

    // Create a radial gradient
    float dist = length(uv - 0.5);
    vec3 color1 = vec3(0.2, 0.4, 0.8); // Blue
    vec3 color2 = vec3(0.8, 0.3, 0.5); // Pink
    vec3 color = mix(color1, color2, dist * 2.0);

    FragColor = vec4(color, 0.5); // Semi-transparent so you can see through it
}
