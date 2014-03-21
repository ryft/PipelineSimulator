package uk.co.ryft.pipeline.model.element.drawable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.PipelineRenderer;
import uk.co.ryft.pipeline.model.lighting.LightingModel;

public class GL_Primitive implements Drawable {

    protected float[] mPositions;
    public float[] mColour;

    protected float[] mColours;
    protected float[] mNormals;

    public FloatBuffer mVertexBuffer;
    public FloatBuffer mColourBuffer;
    public FloatBuffer mNormalBuffer;

    public final int mVertexCount;
    public final int mVertexStride = PipelineRenderer.COORDS_PER_VERTEX * PipelineRenderer.BYTES_PER_FLOAT;
    public final int mPrimitiveType;

    public GL_Primitive(float[] coords, float[] colour, int primitiveType) {

        mVertexCount = coords.length / PipelineRenderer.COORDS_PER_VERTEX;

        mPositions = coords;

        mColour = colour;
        mColours = new float[mVertexCount * PipelineRenderer.COORDS_PER_COLOUR];
        for (int i = 0; i < mVertexCount * PipelineRenderer.COORDS_PER_COLOUR; i++)
            mColours[i] = colour[i % PipelineRenderer.COORDS_PER_COLOUR];

        calculateNormals();

        mPrimitiveType = primitiveType;

        // Initialise vertex byte buffer for shape coordinates
        ByteBuffer vertexBuffer = ByteBuffer.allocateDirect(mPositions.length * PipelineRenderer.BYTES_PER_FLOAT);
        // use the device hardware's native byte order
        vertexBuffer.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = vertexBuffer.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        mVertexBuffer.put(mPositions);
        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);

        // Colour and normal direction buffers used for Lambertian reflectance

        ByteBuffer colourBuffer = ByteBuffer.allocateDirect(mColours.length * PipelineRenderer.BYTES_PER_FLOAT);
        colourBuffer.order(ByteOrder.nativeOrder());
        mColourBuffer = colourBuffer.asFloatBuffer();
        mColourBuffer.put(mColours);
        mColourBuffer.position(0);

        ByteBuffer normalBuffer = ByteBuffer.allocateDirect(mNormals.length * PipelineRenderer.BYTES_PER_FLOAT);
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

        mNormals = new float[mVertexCount * PipelineRenderer.COORDS_PER_VERTEX];
        for (int i = 0; i < mVertexCount * PipelineRenderer.COORDS_PER_VERTEX; i++)
            mNormals[i] = normal[i % PipelineRenderer.COORDS_PER_VERTEX];

    }

    @Override
    public void draw(LightingModel lightingModel, float[] mvMatrix, float[] mvpMatrix) {
        lightingModel.draw(this, mvMatrix, mvpMatrix);
    }
}