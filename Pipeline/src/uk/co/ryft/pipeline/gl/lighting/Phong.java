package uk.co.ryft.pipeline.gl.lighting;

public class Phong extends InterpolatedLighting {
    
    protected Phong() {
        super(Model.PHONG);
    }

    @Override
    // Define a vertex shader for 2D and 3D primitives
    // 2D primitives have uniform lighting attenuated over distance
    // This is because 2D primitives have no notion of a normal direction
    public String getVertexShader(int primitiveType) {
        return   "uniform mat4 u_MVPMatrix;      \n" // Constants representing the combined model-view matrices
                + "uniform mat4 u_MVMatrix;       \n"
                + "                               \n"
                + "attribute vec4 a_Position;     \n" // Per-vertex information we will pass in
                + "attribute vec4 a_Color;        \n"
                + "attribute vec3 a_Normal;       \n"
                + "                               \n"
                + "varying vec3 v_Position;       \n" // Variables to be passed into the fragment shader
                + "varying vec4 v_Color;          \n"
                + "varying vec3 v_Normal;         \n"
                + "                               \n"
                + "void main() {                  \n"
                + "                                                           \n"
                + "    v_Position = vec3(u_MVMatrix * a_Position);            \n" // Transform the vertex into eye space
                + "    v_Color = a_Color;                                     \n" // Pass through the colour
                + "    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n" // Transform the normal's orientation into eye space
                + "                                                           \n"
                + "    gl_Position = u_MVPMatrix * a_Position;                \n" // Project the point into normalised screen coordinates
                + "}                                                          \n";
    }

    @Override
    // XXX Same as Lambertian vertex shader; ignore normal direction and use uniform shading for 2D primitives
    public String getFragmentShader(int primitiveType) {
        
        if (types2D.contains(primitiveType))
            return   "precision mediump float;       \n"
                    + "                               \n"
                    + "uniform vec3 u_LightPos;       \n"     // The position of the light in eye space
                    + "                               \n"
                    + "varying vec3 v_Position;       \n"     // Interpolated position for this fragment
                    + "varying vec4 v_Color;          \n"     // This is the colour from the vertex shader interpolated across the triangle per fragment
                    + "varying vec3 v_Normal;         \n"     // Interpolated normal for this fragment
                    + "                               \n"
                    + "void main() {                  \n"
                    + "                                                                       \n"
                    + "    float distance = length(u_LightPos - v_Position);                  \n" // Will be used for attenuation
                    + "    vec3 lightVector = normalize(u_LightPos - v_Position);             \n" // Get a lighting direction vector from the light to the vertex
                    + "                                                                       \n"
                    + "    float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n" // Calculate the Lambertian reflectance coefficient
                    + "    diffuse = diffuse * (15.0 / (1.0 + (distance * distance)));        \n" // Add attenuation using the inverse square law
                    + "    float ambient = 0.25;                                              \n" // Add an ambient lighting level term
                    + "                                                                       \n"
                    + "    gl_FragColor = v_Color * (diffuse + ambient);                      \n" // Multiply the colour by the diffuse illumination level
                    + "}                                                                      \n";
        
        else
            return   "precision mediump float;       \n"
                    + "                               \n"
                    + "uniform vec3 u_LightPos;       \n"     // The position of the light in eye space
                    + "                               \n"
                    + "varying vec3 v_Position;       \n"     // Interpolated position for this fragment
                    + "varying vec4 v_Color;          \n"     // This is the colour from the vertex shader interpolated across the triangle per fragment
                    + "varying vec3 v_Normal;         \n"     // Interpolated normal for this fragment
                    + "                               \n"
                    + "void main() {                  \n"
                    + "                                                                       \n"
                    + "    float distance = length(u_LightPos - v_Position);                  \n" // Will be used for attenuation
                    + "                                                                       \n"
                    + "    float diffuse = 10.0 / (1.0 + (distance * distance));              \n" // Add attenuation using the inverse square law
                    + "    float ambient = 0.1;                                               \n" // Add an ambient lighting level term
                    + "                                                                       \n"
                    + "    gl_FragColor = v_Color * (diffuse + ambient);                      \n" // Multiply the colour by the diffuse illumination level
                    + "}                                                                      \n";
    }
}
