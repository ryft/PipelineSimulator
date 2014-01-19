package uk.co.ryft.pipeline.gl.lighting;

import java.util.Arrays;
import java.util.List;

import android.opengl.GLES20;

public class Lambertian extends LightingModel {
    
    List<Integer> types3D = Arrays.asList(new Integer[] {GLES20.GL_TRIANGLES, GLES20.GL_TRIANGLE_FAN, GLES20.GL_TRIANGLE_STRIP});
    
    protected Lambertian() {
        super(Model.LAMBERTIAN);
    }
    
    private int mProgram3D;
    private int mProgram2D;
    
    @Override
    public int getGLProgram(int primitiveType) {
        
        boolean is3DPrimitive = types3D.contains(primitiveType);
        
        if (is3DPrimitive && mProgram3D == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));        
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());      
            
            String[] attributes = getVertexShaderAttributes();
            mProgram3D = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
            
        } else if (!is3DPrimitive && mProgram2D == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));        
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());      
            
            String[] attributes = getVertexShaderAttributes();
            mProgram2D = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
        }
        
        if (is3DPrimitive)
            return mProgram3D;
        else
            return mProgram2D;
    }

    @Override
    protected String[] getVertexShaderAttributes() {
        return new String[] {"a_Position", "a_Color", "a_Normal"};
    }

    @Override
    public String getVertexShader(int primitiveType) {
        
        if (primitiveType == GLES20.GL_TRIANGLES || primitiveType == GLES20.GL_TRIANGLE_STRIP || primitiveType == GLES20.GL_TRIANGLE_FAN) {
            return   "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"     // A constant representing the combined model/view matrix.
                    + "uniform vec3 u_LightPos;       \n"     // The position of the light source in eye space.
                    + "                               \n"
                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec4 a_Color;        \n"     // Per-vertex colour information we will pass in.
                    + "attribute vec3 a_Normal;       \n"     // Per-vertex normal information we will pass in.
                    + "                               \n"
                    + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.
                    + "                               \n"
                    + "void main() {                  \n"     // The entry point for our vertex shader.
                    + "                               \n"
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
                    + "     diffuse = diffuse * (1.0 / (1.0 + (0.0125 * distance * distance)));  \n"
                    + "                                                                        \n"
                    //      Get the illuminated colour to be interpolated across the shape
                    + "     v_Color = a_Color * diffuse;                                       \n"
                    //      Pass on the position of the projected vertex in screen coordinates
                    + "     gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "}                                                                       \n";
        
        } else {
            return   "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"     // A constant representing the combined model/view matrix.
                    + "uniform vec3 u_LightPos;       \n"     // The position of the light source in eye space.
                    + "                               \n"
                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec4 a_Color;        \n"     // Per-vertex colour information we will pass in.
                    + "attribute vec3 a_Normal;       \n"     // Per-vertex normal information we will pass in.
                    + "                               \n"
                    + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.
                    + "                               \n"
                    + "void main() {                  \n"     // The entry point for our vertex shader.
                    + "                               \n"
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
                    //      Calculate luminosity using inverse square law attenuation
                    + "     float diffuse = 1.0 / (1.0 + (0.0125 * distance * distance));      \n"
                    + "                                                                        \n"
                    //      Get the illuminated colour to be interpolated across the shape
                    + "     v_Color = a_Color * diffuse;                                       \n"
                    //      Pass on the position of the projected vertex in screen coordinates
                    + "     gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "}                                                                       \n";
        }
    }

    @Override
    public String getFragmentShader() {
        return   "precision mediump float;       \n"
                + "varying vec4 v_Color;          \n"
                + "                               \n"
                + "void main()                    \n"
                + "{                              \n"
                + "   gl_FragColor = v_Color;     \n"
                + "}                              \n";
    }
}
