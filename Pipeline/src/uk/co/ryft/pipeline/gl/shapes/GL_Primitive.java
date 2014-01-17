package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import android.opengl.GLES20;

public class GL_Primitive implements Drawable {

    // Number of coordinates per item in the provided array
    protected final int COORDS_PER_VERTEX = 3;
    protected final int COORDS_PER_COLOUR = 4;
    // Bytes in a float
    protected final int BYTES_PER_FLOAT = 4;
    // Bytes between consecutive vertices
    protected final int vertexStride = COORDS_PER_VERTEX * 4;

    protected float[] mPositions;
    protected float[] mColour;

    protected float[] mColours;
    protected float[] mNormals;

    protected FloatBuffer mVertexBuffer;
    protected FloatBuffer mColourBuffer;
    protected FloatBuffer mNormalBuffer;

    private final int mVertexCount;
    private final int mVertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT;
    private final int mPrimitiveType;

    public GL_Primitive(float[] coords, float[] colour, int primitiveType) {

        mVertexCount = coords.length / COORDS_PER_VERTEX;

        mPositions = coords;

        mColour = colour;
        mColours = new float[mVertexCount * COORDS_PER_COLOUR];
        for (int i = 0; i < mVertexCount * COORDS_PER_COLOUR; i++)
            mColours[i] = colour[i % COORDS_PER_COLOUR];

        calculateNormals();

        mPrimitiveType = primitiveType;

        // Initialise vertex byte buffer for shape coordinates
        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(mPositions.length * BYTES_PER_FLOAT);
        // use the device hardware's native byte order
        vertexBuffer.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = vertexBuffer.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(mPositions);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);

        // Colour and normal direction buffers used for Lambertian reflectance

        ByteBuffer colourBuffer = ByteBuffer.allocateDirect(mColours.length * BYTES_PER_FLOAT);
        colourBuffer.order(ByteOrder.nativeOrder());
        mColourBuffer = colourBuffer.asFloatBuffer();
        mColourBuffer.put(mColours);
        mColourBuffer.position(0);

        ByteBuffer normalBuffer = ByteBuffer.allocateDirect(mNormals.length * BYTES_PER_FLOAT);
        normalBuffer.order(ByteOrder.nativeOrder());
        mNormalBuffer = normalBuffer.asFloatBuffer();
        mNormalBuffer.put(mNormals);
        mNormalBuffer.position(0);
    }

    private void calculateNormals() {

        float[] normal = new float[] { 0, 1, 0 };

        if (mPositions.length >= 9) {
            // Primitives are at most 2D shapes (triangle based primitives)
            // We can calculate the normal direction using the cross product of two vectors;

            // However large the 2D polygon, the triangle consisting of the first 3 vertices
            // will have the same surface normal as the whole shape
            Float3 x = new Float3(mPositions[0], mPositions[1], mPositions[2]);
            Float3 y = new Float3(mPositions[3], mPositions[4], mPositions[5]);
            Float3 z = new Float3(mPositions[6], mPositions[7], mPositions[8]);

            Float3 u = y.minus(x);
            Float3 v = z.minus(x);

            normal = u.cross(v).normalised().toArray();
        }

        mNormals = new float[mVertexCount * COORDS_PER_VERTEX];
        for (int i = 0; i < mVertexCount * COORDS_PER_VERTEX; i++)
            mNormals[i] = normal[i % COORDS_PER_VERTEX];

    }

    public void draw(LightingModel lighting, float[] mvMatrix, float[] mvpMatrix) {

        // Add our shader program to the OpenGL ES environment
        int glProgram = lighting.getGLProgram();
        GLES20.glUseProgram(glProgram);

        // Set program handles for drawing
        int mMVPMatrixHandle;
        int mMVMatrixHandle;
        int mLightPosHandle;
        int mPositionHandle;
        int mColourHandle;
        int mNormalHandle;

        switch (lighting.getModel()) {

            case UNIFORM:

                // Get handle to vertex shader's position member
                mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");

                // Enable a handle to the vertices
                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Prepare coordinate data
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride,
                        mVertexBuffer);

                // Get handle to fragment shader's colour member
                mColourHandle = GLES20.glGetUniformLocation(glProgram, "u_Color");

                // Set colour for drawing the primitive
                GLES20.glUniform4fv(mColourHandle, 1, mColour, 0);

                // Get handle to shape's transformation matrix
                mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");

                // Apply the projection and view transformation
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

                // Draw the primitive
                GLES20.glDrawArrays(mPrimitiveType, 0, mVertexCount);

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                
                PipelineRenderer.checkGlError();
                break;

            case LAMBERTIAN:

                // Get handles to shader members
                mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");
                mMVMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVMatrix");
                mLightPosHandle = GLES20.glGetUniformLocation(glProgram, "u_LightPos");
                mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");
                mColourHandle = GLES20.glGetAttribLocation(glProgram, "a_Color");
                mNormalHandle = GLES20.glGetAttribLocation(glProgram, "a_Normal");

                // Pass in the position information
                mVertexBuffer.position(0);
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Pass in the colour information
                mColourBuffer.position(0);
                GLES20.glVertexAttribPointer(mColourHandle, COORDS_PER_COLOUR, GLES20.GL_FLOAT, false, 0, mColourBuffer);

                GLES20.glEnableVertexAttribArray(mColourHandle);

                // Pass in the normal information
                mNormalBuffer.position(0);
                GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mNormalBuffer);

                GLES20.glEnableVertexAttribArray(mNormalHandle);

                // Pass in the model-view matrix
                GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

                // Pass in the combined matrix
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

                // Pass in the light position in eye space
                GLES20.glUniform3f(mLightPosHandle, PipelineRenderer.sLightPosition.getX(),
                        PipelineRenderer.sLightPosition.getY(), PipelineRenderer.sLightPosition.getZ());

                // Draw the primitive
                GLES20.glDrawArrays(mPrimitiveType, 0, mVertexCount);
                
                // Disable the attribute arrays
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                GLES20.glDisableVertexAttribArray(mColourHandle);
                GLES20.glDisableVertexAttribArray(mNormalHandle);
                
                PipelineRenderer.checkGlError();
                break;
                
            case POINT_SOURCE:

                // Get handle to vertex shader's position member
                mPositionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position");
                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Enable a handle to the vertices
                GLES20.glEnableVertexAttribArray(mPositionHandle);

                // Prepare coordinate data
                GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride,
                        mVertexBuffer);

                // Get handle to shape's transformation matrix
                mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix");

                // Apply the projection and view transformation
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

                // Draw the primitive
                GLES20.glDrawArrays(mPrimitiveType, 0, mVertexCount);

                // Disable vertex array
                GLES20.glDisableVertexAttribArray(mPositionHandle);
                
                PipelineRenderer.checkGlError();
                break;

            default:
                throw new UnsupportedOperationException("Cannot draw primitives with lighting model (" + lighting + ")");

        }
    }
}