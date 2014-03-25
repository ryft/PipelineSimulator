package uk.co.ryft.pipeline.model.lighting;

import java.util.Arrays;
import java.util.List;

import uk.co.ryft.pipeline.model.PipelineRenderer;
import uk.co.ryft.pipeline.model.element.drawable.GL_Primitive;
import android.opengl.GLES20;

abstract class InterpolatedLighting extends LightingModel {

    private static final long serialVersionUID = -3397825035311793185L;

    List<Integer> types2D = Arrays
            .asList(new Integer[] { GLES20.GL_TRIANGLES, GLES20.GL_TRIANGLE_FAN, GLES20.GL_TRIANGLE_STRIP });

    // This class allows Lambertian, Phong models to use distinct shaders for 1D and 2D primitives
    protected InterpolatedLighting(Model m) {
        super(m);
    }

    private int mProgram2D;
    private int mProgram1D;

    @Override
    // XXX Compile a separate program for drawing 1D and 2D primitives
    public int getGLProgram(int primitiveType) {

        boolean is2DPrimitive = types2D.contains(primitiveType);

        if (is2DPrimitive && mProgram2D == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(primitiveType));

            String[] attributes = getVertexShaderAttributes();
            mProgram2D = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);

        } else if (!is2DPrimitive && mProgram1D == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(primitiveType));

            String[] attributes = getVertexShaderAttributes();
            mProgram1D = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
        }

        if (is2DPrimitive)
            return mProgram2D;
        else
            return mProgram1D;
    }

    @Override
    protected String[] getVertexShaderAttributes() {
        return new String[] { "a_Position", "a_Color", "a_Normal" };
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
        int mLightLevelHandle;
        int mPositionHandle;
        int mColourHandle;
        int mNormalHandle;

        // Get handles to shader members
        mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(glProgram, "u_LightPos");
        mLightLevelHandle = GLES20.glGetUniformLocation(glProgram, "u_LightLevel");
        mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");
        mColourHandle = GLES20.glGetAttribLocation(glProgram, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(glProgram, "a_Normal");

        // Pass in the position information
        primitive.mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, PipelineRenderer.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0,
                primitive.mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the colour information
        primitive.mColourBuffer.position(0);
        GLES20.glVertexAttribPointer(mColourHandle, PipelineRenderer.COORDS_PER_COLOUR, GLES20.GL_FLOAT, false, 0,
                primitive.mColourBuffer);

        GLES20.glEnableVertexAttribArray(mColourHandle);

        // Pass in the normal information
        primitive.mNormalBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, PipelineRenderer.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0,
                primitive.mNormalBuffer);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // Pass in the model-view matrix
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

        // Pass in the combined matrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Pass in the light position in eye space
        GLES20.glUniform3f(mLightPosHandle, PipelineRenderer.sLightPosition.getX(), PipelineRenderer.sLightPosition.getY(),
                PipelineRenderer.sLightPosition.getZ());

        // Pass in the overall light level for transitions (fading in/out)
        GLES20.glUniform1f(mLightLevelHandle, mLightLevel);

        // Draw the primitive
        GLES20.glDrawArrays(primitive.mPrimitiveType, 0, primitive.mVertexCount);

        // Disable the attribute arrays
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColourHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);

        checkGlError();
    }

    @Override
    public void reset() {
        super.reset();
        mProgram1D = 0;
        mProgram2D = 0;
    }
}
