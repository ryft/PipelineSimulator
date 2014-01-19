package uk.co.ryft.pipeline.gl.shapes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.lighting.LightingModel;

public class GL_Primitive implements Drawable {
    
    // XXX Language- and library-specific constants
    // TODO put these somewhere nice, and only one place
    // Number of coordinates per item in the provided array
    protected static final int COORDS_PER_VERTEX = 3;
    protected static final int COORDS_PER_COLOUR = 4;
    // Bytes in a float
    protected static final int BYTES_PER_FLOAT = 4;
    // Bytes between consecutive vertices
    protected static final int vertexStride = COORDS_PER_VERTEX * 4;

    protected float[] mPositions;
    public float[] mColour;

    protected float[] mColours;
    protected float[] mNormals;

    public FloatBuffer mVertexBuffer;
    public FloatBuffer mColourBuffer;
    public FloatBuffer mNormalBuffer;

    public final int mVertexCount;
    public final int mVertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT;
    public final int mPrimitiveType;

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

    @Override
    public void draw(LightingModel lightingModel, float[] mvMatrix, float[] mvpMatrix) {
        lightingModel.draw(this, mvMatrix, mvpMatrix);
    }
}