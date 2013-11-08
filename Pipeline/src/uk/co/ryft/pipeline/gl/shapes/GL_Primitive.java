package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import android.opengl.GLES20;

public class GL_Primitive implements Drawable {

    // Number of coordinates per vertex in the provided array
    protected final int COORDS_PER_VERTEX = 3;
    // Bytes between consecutive vertices
    protected final int vertexStride = COORDS_PER_VERTEX * 4;

    // Variables set by the subclasses
    // XXX this won't be subclassed
    protected float mCoords[];
    protected float mColour[];

    protected FloatBuffer mVertexBuffer;
    protected ShortBuffer drawListBuffer;
    protected int mProgram;
    protected int mPositionHandle;
    protected int mColourHandle;
    protected int mMVPMatrixHandle;

    protected String mVertexShaderCode = PipelineRenderer.VERTEX_SHADER_EMPTY;
    protected String mFragmentShaderCode = PipelineRenderer.FRAGMENT_SHADER_EMPTY;

    private final int mVertexCount;
    private final int mVertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int mPrimitiveType;

    public GL_Primitive(float[] coords, float[] colour, int vertexCount, int primitiveType) {

        mCoords = coords;
        mColour = colour;
        mPrimitiveType = primitiveType;

        mVertexCount = mCoords.length / COORDS_PER_VERTEX;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (number of coordinate values * 4 bytes per float)
                mCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(mCoords);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);

        int vertexShader = PipelineRenderer.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = PipelineRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                mFragmentShaderCode);

        mProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables

    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                mVertexStride, mVertexBuffer);

        // get handle to fragment shader's vColor member
        mColourHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColourHandle, 1, mColour, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
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