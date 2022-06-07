uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

vec3 ACESFilm( vec3 x )
{
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x*(a*x+b))/(x*(c*x+d)+e), 0.0, 1.0);
}

void mainImage(out vec4 f, in vec2 fc)
{
    f 	= texture2D(iChannel0, fc / iResolution.xy);
    f.rgb = ACESFilm(f.rgb);
    f.a = 1.0;
}

void main() {
    mainImage(gl_FragColor, texCoord*iResolution.xy);
}