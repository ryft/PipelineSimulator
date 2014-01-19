package uk.co.ryft.pipeline.gl.lighting;

import android.opengl.GLES20;
import android.util.Log;

public abstract class LightingModel {
    
    public static enum Model { UNIFORM, LAMBERTIAN, POINT_SOURCE; }

    public static LightingModel UNIFORM = new Uniform();
    public static LightingModel LAMBERTIAN = new Lambertian();
    public static LightingModel POINT_SOURCE = new PointSource();
    
    private final String TAG = "LightingModel";
    
    protected final Model mModel;
    protected int mProgram = 0;
    
    protected LightingModel(Model m) {
        mModel = m;
    }
    
    public Model getModel() {
        return mModel;
    }
    
    public int getGLProgram(int primitiveType) {
        
        if (mProgram == 0) {
            final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader(primitiveType));        
            final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());      
            
            String[] attributes = getVertexShaderAttributes();
            mProgram = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
        }
        
        return mProgram;
    }
    
    protected abstract String[] getVertexShaderAttributes();
    
    public abstract String getVertexShader(int primitiveType);
    
    public abstract String getFragmentShader();
    
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
    protected int compileShader(final int shaderType, final String shaderSource) 
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
    protected int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
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
