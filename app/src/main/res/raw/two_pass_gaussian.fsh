precision highp float;

uniform vec3                iResolution;
uniform vec3                iChannelResolution[2];
uniform float               iGlobalTime;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

#define pow2(x) (x * x)

const float pi = atan(1.0) * 4.0;
const int samples = 35;
const float sigma = float(samples) * 0.25;

float gaussian(vec2 i) {
    return 1.0 / (2.0 * pi * pow2(sigma)) * exp(-((pow2(i.x) + pow2(i.y)) / (2.0 * pow2(sigma))));
}

vec3 blur(sampler2D sp, vec2 uv, vec2 scale) {
    vec3 col = vec3(0.0);
    float accum = 0.0;
    float weight;
    vec2 offset;

    for (int x = -samples / 2; x < samples / 2; ++x) {
        for (int y = -samples / 2; y < samples / 2; ++y) {
            offset = vec2(x, y);
            weight = gaussian(offset);
            col += texture2D(sp, uv + scale * offset).rgb * weight;
            accum += weight;
        }
    }

    return col / accum;
}

void mainImage(out vec4 color, vec2 coord) {
    vec2 ps = vec2(1.0) / iResolution.xy;
    vec2 uv = coord * ps;

    color.rgb = blur(iChannel0, uv, ps);
    color.a = 1.0;
}

void main() {
    mainImage(gl_FragColor, texCoord*iResolution.xy);
}