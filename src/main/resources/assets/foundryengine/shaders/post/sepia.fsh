#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float r = dot(color.rgb, vec3(0.393, 0.769, 0.189));
    float g = dot(color.rgb, vec3(0.349, 0.686, 0.168));
    float b = dot(color.rgb, vec3(0.272, 0.534, 0.131));
    fragColor = vec4(r, g, b, color.a);
}
