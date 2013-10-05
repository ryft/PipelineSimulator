
package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Drawable;
import android.opengl.GLES20;

public class Point extends Drawable {

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer drawListBuffer;
    protected final int mProgram;
    protected int mPositionHandle;
    protected int mColorHandle;
    protected int mMVPMatrixHandle;
    
    protected short mDrawOrder[];

    public Point(float[] coords, float[] colour, int vertexCount) {

        mCoords = coords;
        mColour = colour;
        mVertexCount = vertexCount;
        
        int drawnPoints = (vertexCount-2)*3;
        mDrawOrder = new short[drawnPoints];
        
        for (int i = 0; i < drawnPoints/3; i++) {
            mDrawOrder[i*3] = 0;
            mDrawOrder[i*3+1] = (short) (i+1);
            mDrawOrder[i*3+2] = (short) (i+2);
        }

        // Initialise vertex byte buffer for shape coordinates
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(mCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(mCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // Initialise byte buffer for the draw list
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(mDrawOrder);
        drawListBuffer.position(0);

        // Create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // Load the shaders from the code
        int vertexShader = PipelineRenderer.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = PipelineRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                mFragmentShaderCode);

        // Add the shaders
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        // Create OpenGL ES executables
        GLES20.glLinkProgram(mProgram);

    }

    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set colour for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColour, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        PipelineRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        PipelineRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the polygon
        GLES20.glDrawElements(GLES20.GL_POINTS, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

    }

}