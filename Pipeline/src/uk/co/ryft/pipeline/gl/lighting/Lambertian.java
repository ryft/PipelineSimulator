package uk.co.ryft.pipeline.gl.lighting;

import java.util.Arrays;
import java.util.List;

import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.gl.shapes.GL_Primitive;
import android.opengl.GLES20;

public class Lambertian extends LightingModel {
    
    List<Integer> types3D = Arrays.asList(new Integer[] {GLES20.GL_TRIANGLES, GLES20.GL_TRIANGLE_FAN, GLES20.GL_TRIANGLE_STRIP});
    
    protected Lambertian() {
        super(Model.LAMBERTIAN);
    }
    
    private int mProgram3D;
    private int mProgram2D;
    
    @Override
    // Compile a separate program for drawing 2D and 3D primitives
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
    // Define a vertex shader for 2D and 3D primitives
    // 2D primitives have uniform lighting attenuated over distance
    // This is because 2D primitives have no notion of a normal direction
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
                    + "     diffuse = diffuse * (15.0 / (1.0 + (distance * distance)));        \n"
                    //      Add an ambient lighting level term
                    + "     float ambient = 0.25;                                              \n"
                    + "                                                                        \n"
                    //      Get the illuminated colour to be interpolated across the shape
                    + "     v_Color = a_Color * (diffuse + ambient);                           \n"
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

    @Override
    public void draw(GL_Primitive primitive, float[] mvMatrix, float[] mvpMatrix) {

        // Add our shader program to the OpenGL ES environment
        int glProgram = getGLProgram(primitive.mPrimitiveType);
        GLES20.glUseProgram(glProgram);

        // Set program handles for drawing
        int mMVPMatrixHandle;
        int mMVMatrixHandle;
        int mLightPosHandle;
        int mPositionHandle;
        int mColourHandle;
        int mNormalHandle;

        // Get handles to shader members
        mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(glProgram, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");
        mColourHandle = GLES20.glGetAttribLocation(glProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(glProgram, "a_Normal");

        // Pass in the position information
        primitive.mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, primitive.mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the colour information
        primitive.mColourBuffer.position(0);
        GLES20.glVertexAttribPointer(mColourHandle, COORDS_PER_COLOUR, GLES20.GL_FLOAT, false, 0, primitive.mColourBuffer);

        GLES20.glEnableVertexAttribArray(mColourHandle);

        // Pass in the normal information
        primitive.mNormalBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, primitive.mNormalBuffer);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // Pass in the model-view matrix
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        // Pass in the combined matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Pass in the light position in eye space
        GLES20.glUniform3f(mLightPosHandle, PipelineRenderer.sLightPosition.getX(),
                PipelineRenderer.sLightPosition.getY(), PipelineRenderer.sLightPosition.getZ());

        // Draw the primitive
        GLES20.glDrawArrays(primitive.mPrimitiveType, 0, primitive.mVertexCount);
        
        // Disable the attribute arrays
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColourHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        
        PipelineRenderer.checkGlError();
    }
    
    @Override
    public void reset() {
        mProgram2D = 0;
        mProgram3D = 0;
    }
}
