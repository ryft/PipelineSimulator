package uk.co.ryft.pipeline.gl.lighting;

public class Uniform extends LightingModel {
    
    protected Uniform() {
        super(Model.UNIFORM);
    }

    @Override
    protected String[] getVertexShaderAttributes() {
        return new String[] {"a_Position"};
    }

    @Override
    public String getVertexShader(int primitiveType) {
        return   "uniform mat4 u_MVPMatrix;      \n"
                + "attribute vec4 a_Position;     \n"
                + "                               \n"
                + "void main() {                  \n"
                + "                               \n"
                //      The order must be matrix * vector as the matrix is in col-major order.
                + "     gl_Position = u_MVPMatrix * a_Position; \n"
                + "}                              \n";
    }

    @Override
    public String getFragmentShader() {
        return   "precision mediump float;       \n"
                + "uniform vec4 u_Color;          \n"
                + "                               \n"
                + "void main() {                  \n"
                + "    gl_FragColor = u_Color;    \n"
                + "}                              \n";
    }
}
