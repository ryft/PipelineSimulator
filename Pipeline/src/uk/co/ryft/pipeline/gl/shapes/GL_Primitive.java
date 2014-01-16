package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import android.opengl.GLES20;

public class GL_Primitive implements Drawable {

    // Number of coordinates per vertex in the provided array
    protected final int COORDS_PER_VERTEX = 3;
    // Bytes between consecutive vertices
    protected final int vertexStride = COORDS_PER_VERTEX * 4;

    protected float[] mPositions;
    protected float[] mColour;

    protected FloatBuffer mVertexBuffer;
    
    protected int mPositionHandle;
    protected int mColourHandle;
    protected int mMVPMatrixHandle;

    private final int mVertexCount;
    private final int mVertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int mPrimitiveType;

    public GL_Primitive(float[] coords, float[] colour, int vertexCount, int primitiveType) {

        mPositions = coords;
        mColour = colour;
        mPrimitiveType = primitiveType;

        mVertexCount = mPositions.length / COORDS_PER_VERTEX;

        // Initialise vertex byte buffer for shape coordinates
        ByteBuffer vb = ByteBuffer.allocateDirect(mPositions.length * 4);
        // use the device hardware's native byte order
        vb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = vb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(mPositions);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);

    }

    public void draw(int glProgram, float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(glProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                mVertexStride, mVertexBuffer);

        // get handle to fragment shader's vColor member
        mColourHandle = GLES20.glGetUniformLocation(glProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColourHandle, 1, mColour, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "uMVPMatrix");
        PipelineRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        PipelineRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the line(s)
        // TODO: Consider using glDrawElements
        GLES20.glDrawArrays(mPrimitiveType, 0, mVertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}