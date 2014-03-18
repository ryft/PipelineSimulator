package uk.co.ryft.pipeline.model.lighting;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.co.ryft.pipeline.model.element.drawable.GL_Primitive;
import android.opengl.GLES20;
import android.util.Log;

public abstract class LightingModel implements Serializable {

    private static final long serialVersionUID = -8281004338010378525L;

    private static final String TAG = "LightingModel";

    // XXX Language- and library-specific constants
    // TODO put these somewhere nice, and only one place
    // Number of coordinates per item in the provided array
    protected static final int COORDS_PER_VERTEX = 3;
    protected static final int COORDS_PER_COLOUR = 4;
    // Bytes in a float
    protected static final int BYTES_PER_FLOAT = 4;
    // Bytes between consecutive vertices
    protected static final int vertexStride = COORDS_PER_VERTEX * 4;

    public static enum Model {
        UNIFORM, LAMBERTIAN, PHONG, POINT_SOURCE;

        private static final Map<Model, String> mTitleMap;
        static {
            Map<Model, String> titleMap = new HashMap<Model, String>();
            titleMap.put(Model.UNIFORM, "Uniform lighting");
            titleMap.put(Model.LAMBERTIAN, "Lambertian reflectance");
            titleMap.put(Model.PHONG, "Phong shading");
            titleMap.put(Model.POINT_SOURCE, "Point light source");
            mTitleMap = Collections.unmodifiableMap(titleMap);
        }

        public String getTitle() {
            return mTitleMap.get(this);
        }
    }

    protected final Model mModel;
    protected int mProgram = 0;

    protected LightingModel(Model m) {
        mModel = m;
    }

    public Model getModel() {
        return mModel;
    }

    public static LightingModel getLightingModel(Model model) {
        switch (model) {
            case UNIFORM:
                return new Uniform();
            case LAMBERTIAN:
                return new Lambertian();
            case PHONG:
                return new Phong();
            case POINT_SOURCE:
                return new PointSource();
            default:
                return null;
        }
    }

    public int getGLProgram(int primitiveType) {

        if (mProgram == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader(primitiveType));

            String[] attributes = getVertexShaderAttributes();
            mProgram = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
        }

        return mProgram;
    }

    protected float mLightLevel = 1;

    public void setGlobalLightLevel(float lightLevel) {
        // TODO consider using alpha values here -- then we could do a convincing crossfade.
        mLightLevel = lightLevel;
    }

    protected abstract String[] getVertexShaderAttributes();

    public abstract String getVertexShader(int primitiveType);

    public abstract String getFragmentShader(int primitiveType);

    public abstract void draw(GL_Primitive primitive, float[] mvMatrix, float[] mvpMatrix);

    // XXX Clears the GL program for use in a new render thread
    public void reset() {
        mProgram = 0;
        setGlobalLightLevel(1);
    }

    @Override
    public String toString() {
        return mModel.getTitle();
    }

    /**
     * Helper function to compile a shader.
     * 
     * @param shaderType
     *            The shader type.
     * @param shaderSource
     *            The shader source code.
     * @return An OpenGL handle to the shader.
     */
    protected int compileShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * Helper function to compile and link a program.
     * 
     * @param vertexShaderHandle
     *            An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle
     *            An OpenGL handle to an already-compiled fragment shader.
     * @param attributes
     *            Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    protected int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    /**
     * Utility method for debugging OpenGL calls:
     * 
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
     * MyGLRenderer.checkGlError();
     * </pre>
     * 
     * If the operation is not successful, the check throws an error.
     */
    public void checkGlError() {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glError " + error);
            throw new RuntimeException("glError " + error);
        }
    }

}
