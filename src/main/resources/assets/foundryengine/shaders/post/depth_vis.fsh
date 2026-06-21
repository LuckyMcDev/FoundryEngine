#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 sceneColor = texture(MainSampler, texCoord);
    float depth = texture(MainDepthSampler, texCoord).r;

    vec3 col;
    col = mix(vec3(0.1, 0.0, 0.5), vec3(0.0, 0.0, 1.0), smoothstep(0.0, 0.25, depth));
    col = mix(col, vec3(0.0, 1.0, 1.0), smoothstep(0.25, 0.5, depth));
    col = mix(col, vec3(1.0, 1.0, 0.0), smoothstep(0.5, 0.75, depth));
    col = mix(col, vec3(1.0, 0.2, 0.0), smoothstep(0.75, 1.0, depth));

    fragColor = vec4(col, 1.0);
}
