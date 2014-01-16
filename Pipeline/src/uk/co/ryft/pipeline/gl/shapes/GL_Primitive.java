package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.shapes.Primitive;
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

    protected int mPositionHandle;
    protected int mColourHandle;
    protected int mMVPMatrixHandle;

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

    private void calculateNormals() {

        float[] normal = new float[] { 0, 1, 0 };

        if (mPositions.length >= 3) {
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

    public void draw(int glProgram, float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(glProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);

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