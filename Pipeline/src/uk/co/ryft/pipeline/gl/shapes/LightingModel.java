package uk.co.ryft.pipeline.gl.shapes;

import android.opengl.GLES20;
import android.util.Log;

public class LightingModel {
    
    public static enum Model { UNIFORM, LAMBERTIAN, POINT_SOURCE; }

    public static LightingModel UNIFORM = new LightingModel(Model.UNIFORM);
    public static LightingModel LAMBERTIAN = new LightingModel(Model.LAMBERTIAN);
    public static LightingModel POINT_SOURCE = new LightingModel(Model.POINT_SOURCE);
    
    private final String TAG = "LightingModel";
    
    private final Model mModel;
    private int mProgram = 0;
    
    private LightingModel(Model m) {
        mModel = m;
    }
    
    public Model getModel() {
        return mModel;
    }
    
    public int getGLProgram() {
        
        if (mProgram == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader());        
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());      
            
            String[] attributes = getVertexShaderAttributes();
            mProgram = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
        }
        
        return mProgram;
    }
    
    private String[] getVertexShaderAttributes() {
        
        switch(mModel) {
            case UNIFORM:
                return new String[] {"a_Position"};
            case LAMBERTIAN:
                return new String[] {"a_Position", "a_Color", "a_Normal"};
            case POINT_SOURCE:
                return new String[] {"a_Position"};
            default:
                throw new UnsupportedOperationException("No vertex shader attributes exist for lighting model (" + toString() + ")");
        }
    }
    
    public String getVertexShader() {
        
        switch (mModel) {
            case UNIFORM:
                return   "uniform mat4 u_MVPMatrix;      \n"
                        + "attribute vec4 a_Position;     \n"
                        + "                               \n"
                        + "void main() {                  \n"
                        + "                               \n"
                        //      The order must be matrix * vector as the matrix is in col-major order.
                        + "     gl_Position = u_MVPMatrix * a_Position; \n"
                        + "}                              \n";
        
            case LAMBERTIAN:
                return   "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
                        + "uniform mat4 u_MVMatrix;       \n"     // A constant representing the combined model/view matrix.
                        + "uniform vec3 u_LightPos;       \n"     // The position of the light source in eye space.
                        + "                               \n"
                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"     // Per-vertex colour information we will pass in.
                        + "attribute vec3 a_Normal;       \n"     // Per-vertex normal information we will pass in.
                        + "                               \n"
                        + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.
                        + "                               \n"
                        + "void main() {                  \n"     // The entry point for our vertex shader.
                        + "                               \n"
                        //      Calculate the position of the vertex in eye space
                        + "     vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                        //      Transform the normal direction into eye space
                        + "     vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                        + "                                                                        \n"
                        //      Calculate the distance between the light and the vertex for attenuation
                        + "     float distance = length(u_LightPos - modelViewVertex);             \n"
                        //      Get the lighting direction vector from the light source to the vertex
                        + "     vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
                        + "                                                                        \n"
                        //      Calculate the Lambertian reflectance coefficient
                        + "     float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"
                        //      Calculate luminosity using inverse square law attenuation
                        + "     diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
                        + "                                                                        \n"
                        //      Get the illuminated colour to be interpolated across the shape
                        + "     v_Color = a_Color * diffuse;                                       \n"
                        //      Pass on the position of the projected vertex in screen coordinates
                        + "     gl_Position = u_MVPMatrix * a_Position;                            \n"
                        + "}                                                                       \n";
                
            case POINT_SOURCE:
                return   "uniform mat4 u_MVPMatrix;      \n"
                        + "attribute vec4 a_Position;     \n"
                        + "                               \n"
                        + "void main() {                  \n"
                        + "                               \n"
                        + "    gl_Position = u_MVPMatrix  \n"
                        + "                * a_Position;  \n"
                        + "    gl_PointSize = 10.0;        \n"
                        + "}                              \n";
                
            default:
                throw new UnsupportedOperationException("No vertex shader exists for lighting model (" + toString() + ")");
        }
    }
    
    public String getFragmentShader() {
        
        switch (mModel) {
            case UNIFORM:
                return   "precision mediump float;       \n"
                        + "uniform vec4 u_Color;          \n"
                        + "                               \n"
                        + "void main() {                  \n"
                        + "    gl_FragColor = u_Color;    \n"
                        + "}                              \n";
                
            case LAMBERTIAN:
                return   "precision mediump float;       \n"
                        + "varying vec4 v_Color;          \n"
                        + "                               \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"
                        + "}                              \n";
                
            case POINT_SOURCE:
                return   "precision mediump float;       \n"
                        + "void main() {                  \n"
                        + "                               \n"
                        + "    gl_FragColor = vec4(1.0,   \n"
                        + "    1.0, 1.0, 1.0);            \n"
                        + "}                              \n";
                
            default:
                throw new UnsupportedOperationException("No fragment shader exists for lighting model (" + toString() + ")");
        }
    }
    
    @Override
    public String toString() {
        return mModel.name();
    }
    
    /** 
     * Helper function to compile a shader.
     * 
     * @param shaderType The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    private int compileShader(final int shaderType, final String shaderSource) 
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) 
        {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) 
            {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0)
        {           
            throw new RuntimeException("Error creating shader.");
        }
        
        return shaderHandle;
    }   
    
    /**
     * Helper function to compile and link a program.
     * 
     * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
    {
        int programHandle = GLES20.glCreateProgram();
        
        if (programHandle != 0) 
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);           

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            
            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }                       
            }
            
            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) 
            {               
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        
        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }
        
        return programHandle;
    }

}
