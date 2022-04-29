#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float PlacementDelta;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    float value = sin(PlacementDelta / 4.0f) / 4.0f + 0.75f;
    vec4 color = texture(Sampler0, texCoord0);
    color.a *= value;
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
