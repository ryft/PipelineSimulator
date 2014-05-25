package uk.co.ryft.pipeline.model.lighting;

class Lambertian extends InterpolatedLighting {
    
    private static final long serialVersionUID = -5547031387655285667L;

    protected Lambertian() {
        super(ModelType.LAMBERTIAN);
    }

    @Override
    // Define a vertex shader for 2D and 3D primitives
    // 2D primitives have uniform lighting attenuated over distance
    // This is because 2D primitives have no notion of a normal direction
    public String getVertexShader(int primitiveType) {
        
        if (types2D.contains(primitiveType))
            return   "uniform mat4 u_MVPMatrix;      \n" // Constants representing the combined model-view matrices
                    + "uniform mat4 u_MVMatrix;       \n"
                    + "uniform vec3 u_LightPos;       \n" // The position of the light source in eye space
                    + "                               \n"
                    + "attribute vec4 a_Position;     \n" // Per-vertex information we will pass in
                    + "attribute vec4 a_Color;        \n"
                    + "attribute vec3 a_Normal;       \n"
                    + "                               \n"
                    + "varying vec4 v_Color;          \n" // Variable to be passed into the fragment shader
                    + "                               \n"
                    + "void main() {                  \n"
                    + "                                                                        \n"
                    //      Calculate the position of the vertex in eye space
                    + "     vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                    //      Transform the normal direction into eye space
                    + "     vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                    + "                                                                        \n"
                    //      Calculate the distance between the light and the vertex for attenuation
                    + "     float distance = length(u_LightPos - modelViewVertex);             \n"
                    //      Get the lighting direction vector from the light source to the vertex
                    + "     vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
                    + "                                                                        \n"
                    //      Calculate the Lambertian reflectance coefficient
                    + "     float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"
                    //      Calculate luminosity using inverse square law attenuation
                    + "     diffuse = diffuse * (15.0 / (1.0 + (distance * distance)));        \n"
                    //      Add an ambient lighting level term
                    + "     float ambient = 0.25;                                              \n"
                    + "                                                                        \n"
                    //      Get the illuminated colour to be interpolated across the shape
                    + "     v_Color = a_Color * (diffuse + ambient);                           \n"
                    //      Pass on the position of the projected vertex in screen coordinates
                    + "     gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "}                                                                       \n";
        
        else
            return   "uniform mat4 u_MVPMatrix;      \n" // Constants representing the combined model-view matrices
                    + "uniform mat4 u_MVMatrix;       \n"
                    + "uniform vec3 u_LightPos;       \n" // The position of the light source in eye space
                    + "                               \n"
                    + "attribute vec4 a_Position;     \n" // Per-vertex information we will pass in
                    + "attribute vec4 a_Color;        \n"
                    + "attribute vec3 a_Normal;       \n"
                    + "                               \n"
                    + "varying vec4 v_Color;          \n" // Variable to be passed into the fragment shader
                    + "                               \n"
                    + "void main() {                  \n"
                    + "                                                                        \n"
                    //      Calculate the position of the vertex in eye space
                    + "     vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                    //      Transform the normal direction into eye space
                    + "     vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                    + "                                                                        \n"
                    //      Calculate the distance between the light and the vertex for attenuation
                    + "     float distance = length(u_LightPos - modelViewVertex);             \n"
                    + "                                                                        \n"
                    //      Calculate luminosity using inverse square law attenuation
                    + "     float diffuse = 10.0 / (1.0 + (distance * distance));              \n"
                    //      Ambient coefficient is penalised here to compensate for the uniformity
                    + "     float ambient = 0.1;                                               \n"
                    + "                                                                        \n"
                    //      Get the illuminated colour to be interpolated across the shape
                    + "     v_Color = a_Color * (diffuse + ambient);                           \n"
                    //      Pass on the position of the projected vertex in screen coordinates
                    + "     gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "}                                                                       \n";
    }

    @Override
    public String getFragmentShader(int primitiveType) {
        return   "precision mediump float;                     \n"
                + "uniform float u_LightLevel;                  \n" // Light level parameter for use in transition animations
                + "varying vec4 v_Color;                        \n"
                + "                                             \n"
                + "void main() {                                \n"
                + "   gl_FragColor = v_Color * u_LightLevel;    \n"
                + "}                                            \n";
    }
}
