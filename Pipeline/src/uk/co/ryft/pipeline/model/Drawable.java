
package uk.co.ryft.pipeline.model;


public abstract class Drawable {

//    protected final int COORDS_PER_VERTEX;
//
//    // Bytes between consecutive vertices
//    protected final int VERTEX_STRIDE;
//    protected float mColourArray[];
//    protected short mDrawOrder[];
//
//    protected final FloatBuffer mVertexBuffer;
//    protected final ShortBuffer mDrawListBuffer;
//
//    protected final int mProgram;
//
//    protected static final String VERTEX_SHADER_EMPTY =
//            // This matrix member variable provides a hook to manipulate
//            // the coordinates of the objects that use this vertex shader
//            "uniform mat4 uMVPMatrix;" +
//
//                    "attribute vec4 vPosition;" +
//                    "void main() {" +
//                    // the matrix must be included as a modifier of gl_Position
//                    "  gl_Position = vPosition * uMVPMatrix;" +
//                    "}";
//
//    protected static final String FRAGMENT_SHADER_EMPTY =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    "  gl_FragColor = vColor;" +
//                    "}";
//
//    protected String mVertexShaderCode = VERTEX_SHADER_EMPTY;
//    protected String mFragmentShaderCode = FRAGMENT_SHADER_EMPTY;
//
//    public Drawable(float[] coords, float[] colour, int vertexCount) {
//
//        COORDS_PER_VERTEX = coords.length / vertexCount;
//        VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
//
//        int drawnPoints = (vertexCount - 2) * 3;
//        mDrawOrder = new short[drawnPoints];
//
//        mColourArray = colour;
//
//        for (int i = 0; i < drawnPoints / 3; i++) {
//            mDrawOrder[i * 3] = 0;
//            mDrawOrder[i * 3 + 1] = (short) (i + 1);
//            mDrawOrder[i * 3 + 2] = (short) (i + 2);
//        }
//
//        // Initialise vertex byte buffer for shape coordinates
//        // (number of coordinate values * 4 bytes per float)
//        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
//        // use the device hardware's native byte order
//        bb.order(ByteOrder.nativeOrder());
//
//        // create a floating point buffer from the ByteBuffer
//        mVertexBuffer = bb.asFloatBuffer();
//        // add the coordinates to the FloatBuffer
//        mVertexBuffer.put(coords);
//        // set the buffer to read the first coordinate
//        mVertexBuffer.position(0);
//
//        // Initialise byte buffer for the draw list
//        // (# of coordinate values * 2 bytes per short)
//        ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawOrder.length * 2);
//        dlb.order(ByteOrder.nativeOrder());
//        mDrawListBuffer = dlb.asShortBuffer();
//        mDrawListBuffer.put(mDrawOrder);
//        mDrawListBuffer.position(0);
//
//        // Create empty OpenGL ES Program
//        mProgram = GLES20.glCreateProgram();
//
//        // Load the shaders from the code
//        int vShader = PipelineRenderer.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
//        int fShader = PipelineRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
//
//        // Add the shaders
//        GLES20.glAttachShader(mProgram, vShader);
//        GLES20.glAttachShader(mProgram, fShader);
//
//        // Create OpenGL ES executables
//        GLES20.glLinkProgram(mProgram);
//    }
//
//    public void applyShaders(String vertexShaderCode, String fragmentShaderCode) {
//        mVertexShaderCode = vertexShaderCode;
//        mFragmentShaderCode = fragmentShaderCode;
//    }

    public abstract void draw(float[] mvpMatrix);

}
