package uk.co.ryft.pipeline.gl.lighting;

import uk.co.ryft.pipeline.gl.shapes.GL_Primitive;
import android.opengl.GLES20;

public class Uniform extends LightingModel {
    
    private static final long serialVersionUID = -2930605266784494521L;

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
                + "                                             \n"
                //     The order must be matrix * vector as the matrix is in col-major order
                + "    gl_Position = u_MVPMatrix * a_Position;  \n"
                + "}                                            \n";
    }

    @Override
    public String getFragmentShader(int primitiveType) {
        return   "precision mediump float;       \n"
                + "uniform vec4 u_Color;          \n"
                + "                               \n"
                + "void main() {                  \n"
                + "    gl_FragColor = u_Color;    \n"
                + "}                              \n";
    }

    @Override
    public void draw(GL_Primitive primitive, float[] mvMatrix, float[] mvpMatrix) {

        // Add our shader program to the OpenGL ES environment
        int glProgram = getGLProgram(primitive.mPrimitiveType);
        GLES20.glUseProgram(glProgram);

        // Set program handles for drawing
        int mMVPMatrixHandle;
        int mPositionHandle;
        int mColourHandle;

        // Get handle to vertex shader's position member
        mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, primitive.mVertexStride,
                primitive.mVertexBuffer);

        // Get handle to fragment shader's colour member
        mColourHandle = GLES20.glGetUniformLocation(glProgram, "u_Color");

        // Set colour for drawing the primitive
        GLES20.glUniform4fv(mColourHandle, 1, primitive.mColour, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the primitive
        GLES20.glDrawArrays(primitive.mPrimitiveType, 0, primitive.mVertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        
        checkGlError();
    }
}
