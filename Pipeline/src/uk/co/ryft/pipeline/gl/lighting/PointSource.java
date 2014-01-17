package uk.co.ryft.pipeline.gl.lighting;

public class PointSource extends LightingModel {
    
    protected PointSource() {
        super(Model.POINT_SOURCE);
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
                + "    gl_Position = u_MVPMatrix  \n"
                + "                * a_Position;  \n"
                + "    gl_PointSize = 10.0;        \n"
                + "}                              \n";
    }

    public String getFragmentShader() {
        return   "precision mediump float;       \n"
                + "void main() {                  \n"
                + "                               \n"
                + "    gl_FragColor = vec4(1.0,   \n"
                + "    1.0, 1.0, 1.0);            \n"
                + "}                              \n";
    }
}
