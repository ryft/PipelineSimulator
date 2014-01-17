package uk.co.ryft.pipeline.gl.lighting;

public class Uniform extends LightingModel {
    
    protected Uniform() {
        super(Model.UNIFORM);
    }
    
    protected String[] getVertexShaderAttributes() {
        return new String[] {"a_Position"};
    }

    public String getVertexShader() {
        return   "uniform mat4 u_MVPMatrix;      \n"
                + "attribute vec4 a_Position;     \n"
                + "                               \n"
                + "void main() {                  \n"
                + "                               \n"
                //      The order must be matrix * vector as the matrix is in col-major order.
                + "     gl_Position = u_MVPMatrix * a_Position; \n"
                + "}                              \n";
    }

    public String getFragmentShader() {
        return   "precision mediump float;       \n"
                + "uniform vec4 u_Color;          \n"
                + "                               \n"
                + "void main() {                  \n"
                + "    gl_FragColor = u_Color;    \n"
                + "}                              \n";
    }
}
