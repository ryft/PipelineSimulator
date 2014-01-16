package uk.co.ryft.pipeline.gl;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.ryft.pipeline.gl.shapes.GL_Primitive;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Rotation;
import uk.co.ryft.pipeline.model.Transformation;
import uk.co.ryft.pipeline.model.Translation;
import uk.co.ryft.pipeline.model.shapes.Composite;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class PipelineRenderer implements Renderer, Serializable {

    private static final long serialVersionUID = -5651858198215667027L;

    private static final String TAG = "PipelineRenderer";
    
    public static boolean LAMBERT = true;

    // OpenGL matrices stored in float arrays (column-major order)
    private final float[] mModelMatrix = new float[16];
    private final float[] mCameraModelMatrix = new float[16];

    private final float[] mViewMatrix = new float[16];
    private final float[] mCameraViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    
    private final float[] mMVMatrix = new float[16];
    private final float[] mCVMatrix = new float[16];
    
    private final float[] mMVPMatrix = new float[16];
    private final float[] mCVPMatrix = new float[16];

    private final Map<Element, Drawable> mElements = new LinkedHashMap<Element, Drawable>();

    private final List<Transformation> mModelTransformations = new LinkedList<Transformation>();
    
    private Camera mActualCamera = new Camera(new Float3(2, 2, 2), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 1, 7);
    private Camera mVirtualCamera = new Camera(new Float3(-1f, 0.5f, 0.5f), new Float3(0, 0, 1), new Float3(0, 1, 0), -0.25f, 0.25f, -0.25f, 0.25f, 0.5f, 1.5f);

    // TODO This is unsafe. If that's OK we can just use public variables,
    // if not implement cloneable and clone each before returning.
    public Camera getActualCamera() { return mActualCamera; }
    public Camera getVirtualCamera() { return mVirtualCamera; }
    public void setActualCamera(Camera actualCamera) { mActualCamera = actualCamera; }
    public void setVirtualCamera(Camera virtualCamera) { mVirtualCamera = virtualCamera; }

    // For touch events
    // TODO: Implement a monitor for this.
    private volatile float mAngle;
    
    public float getRotation() { return mAngle; }
    public void setRotation(float angle) { mAngle = angle; }
    
    private final float[] mModelRotationMatrix = new float[16];

    // Drawables aren't initialised, and are constructed at render time if necessary
    private Composite mCamera = new Composite(Composite.Type.CAMERA, Collections.<Element> emptyList());
    private Drawable mCameraDrawable;

    // Axes should never change between instances so they can be declared statically
    private static Composite sAxes;
    private static Drawable sAxesDrawable;
    static {
        LinkedList<Element> axes = new LinkedList<Element>();

        LinkedList<Float3> lineCoords = new LinkedList<Float3>();
        // XXX i < 1.1 is required to draw the edge lines
        for (float i = -1; i < 1.1; i += 0.1) {
            lineCoords.add(new Float3(i, 0, -1));
            lineCoords.add(new Float3(i, 0, 1));
            lineCoords.add(new Float3(-1, 0, i));
            lineCoords.add(new Float3(1, 0, i));
        }
        axes.add(new Primitive(Primitive.Type.GL_LINES, lineCoords, Colour.GREY));
        
        LinkedList<Float3> points = new LinkedList<Float3>();
        points.add(new Float3(0, 0, 0));
        points.add(new Float3(1, 0, 0));
        points.add(new Float3(0, 0, 0));
        points.add(new Float3(0, 1, 0));
        points.add(new Float3(0, 0, 0));
        points.add(new Float3(0, 0, 1));
        axes.add(new Primitive(Primitive.Type.GL_LINES, points, Colour.WHITE));

        LinkedList<Float3> arrowX = new LinkedList<Float3>();
        arrowX.add(new Float3(0.8f, 0.1f, -0.1f));
        arrowX.add(new Float3(1, 0, 0));
        arrowX.add(new Float3(0.8f, -0.1f, 0.1f));
        arrowX.add(new Float3(0.9f, 0, 0));
        axes.add(new Primitive(Primitive.Type.GL_LINE_LOOP, arrowX, Colour.RED));

        LinkedList<Float3> arrowY = new LinkedList<Float3>();
        arrowY.add(new Float3(-0.1f, 0.8f, 0.1f));
        arrowY.add(new Float3(0, 1, 0));
        arrowY.add(new Float3(0.1f, 0.8f, -0.1f));
        arrowY.add(new Float3(0, 0.9f, 0));
        axes.add(new Primitive(Primitive.Type.GL_LINE_LOOP, arrowY, Colour.GREEN));

        LinkedList<Float3> arrowZ = new LinkedList<Float3>();
        arrowZ.add(new Float3(0.1f, -0.1f, 0.8f));
        arrowZ.add(new Float3(0, 0, 1));
        arrowZ.add(new Float3(-0.1f, 0.1f, 0.8f));
        arrowZ.add(new Float3(0, 0, 0.9f));
        axes.add(new Primitive(Primitive.Type.GL_LINE_LOOP, arrowZ, Colour.BLUE));

        sAxes = new Composite(Composite.Type.CUSTOM, axes);
    }

    public void interact() {
        mModelTransformations.add(new Translation(new Float3(0, 1, 0)));
        mModelTransformations.add(new Rotation(90, new Float3(1, 0, 0)));
    }

    private String getVertexShader() {
        
        if (!LAMBERT)
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            return "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // the order must be matrix * vector as the matrix is in col-major order.
                    "    gl_Position = uMVPMatrix * vPosition;" +
                    "}";
        
        else
            return   "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"     // A constant representing the combined model/view matrix.
                    + "uniform vec3 u_LightPos;       \n"     // The position of the light in eye space.
                   
                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.
                    + "attribute vec3 a_Normal;       \n"     // Per-vertex normal information we will pass in.
                   
                    + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.
                   
                    + "void main()                    \n"     // The entry point for our vertex shader.
                    + "{                              \n"
                    + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              \n"
                    + "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));     \n"
                    + "   float distance = length(u_LightPos - modelViewVertex);             \n"
                    + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        \n"
                    + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"
                    + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"
                    + "   v_Color = a_Color * diffuse;                                       \n"
                    + "   gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "}                                                                     \n";
    }

    private String getFragmentShader() {
        
        if (!LAMBERT)
            return "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "    gl_FragColor = vColor;" +
                    "}";
        
        else
            return   "precision mediump float;       \n"
                    + "varying vec4 v_Color;          \n"
                    + "void main()                    \n"     // The entry point for our fragment shader.
                    + "{                              \n"
                    + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
                    + "}                              \n";
    }
    
    private int mProgram;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        
        // Initialise cameras
        mActualCamera = new Camera(new Float3(2, 2, 2), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 1, 7);
        mVirtualCamera = new Camera(new Float3(-1f, 0.5f, 0.5f), new Float3(0, 0, 1), new Float3(0, 1, 0), -0.25f, 0.25f, -0.25f, 0.25f, 0.5f, 1.5f);
        
        LinkedList<Element> camera = new LinkedList<Element>();
        camera.add(ShapeFactory.buildCamera(0.25f));
        camera.add(ShapeFactory.buildFrustum(mVirtualCamera));
        mCamera = new Composite(Composite.Type.CUSTOM, camera);

        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        
        // Enable depth buffer and set parameters
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        
        // Enable face culling and set parameters
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glFrontFace(GLES20.GL_CCW);
        
        // For touch events
        Matrix.setIdentityM(mModelRotationMatrix, 0);
        
        final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, getVertexShader());        
        final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());      
        
        mProgram = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
                new String[] {"vPosition"});
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);
        
        mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, width, height);
    
    }
    @Override
    public void onDrawFrame(GL10 unused) {

        // Clear background colour and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set up the model (world transformation) matrix
        Matrix.setIdentityM(mModelMatrix, 0);

        // Get the current camera view matrices
        mActualCamera.setViewMatrix(mViewMatrix, 0);
        mVirtualCamera.setViewMatrix(mCameraViewMatrix, 0);
        
        // The camera model matrix transforms the camera to its correct position and orientation in world space
        Matrix.invertM(mCameraModelMatrix, 0, mCameraViewMatrix, 0);

        long time = SystemClock.uptimeMillis();
        
        // Apply all transformations to the world, in order, in their current state
        for (Transformation t : mModelTransformations)
            Matrix.multiplyMM(mModelMatrix, 0, t.getTransformation(time), 0, mModelMatrix, 0);
        
        // Combine the current rotation matrix with the projection and camera view for touch-rotation
        Matrix.setRotateM(mModelRotationMatrix, 0, mAngle, 0, 1, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mModelRotationMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mCameraModelMatrix, 0, mModelRotationMatrix, 0, mCameraModelMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mCVMatrix, 0, mViewMatrix, 0, mCameraModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        Matrix.multiplyMM(mCVPMatrix, 0, mProjectionMatrix, 0, mCVMatrix, 0);

        // Initialise axes and camera drawables if necessary
        // Avoid object construction as much as possible at render time
        if (sAxesDrawable == null)
            sAxesDrawable = sAxes.getDrawable();
        if (mCameraDrawable == null)
            mCameraDrawable = mCamera.getDrawable();

        // Draw axes and virtual camera        
        sAxesDrawable.draw(mProgram, mMVPMatrix);
        mCameraDrawable.draw(mProgram, mCVPMatrix);
        
        cube.draw(mProgram, mMVPMatrix);
        
        // Draw world objects in the scene
        for (Element e : mElements.keySet()) {
            if (mElements.get(e) == null)
                mElements.put(e, e.getDrawable());
            Drawable d = mElements.get(e);
            if (d != null)
                d.draw(mProgram, mMVPMatrix);
            else
                // Occasionally happens when app is quitting
                // TODO: Investigate turning off continuous rendering when quitting
                System.out.println("Ruh-roh, null drawable!");
        }

    }
    
    protected void printMatrix(float[] m, int cols, int rows) {
        for (int i = 0; i < rows; i++) {
            if (i == 0)
                System.out.print("[");
            else
                System.out.print(" ");
            for (int j = 0; j < cols; j++) {
                System.out.print(m[i + (j * 4)] + " ");
            }
            if (i == rows - 1)
                System.out.println("]");
            else
                System.out.println();
        }
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call just after making it:
     * 
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
     * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
     * </pre>
     * 
     * If the operation is not successful, the check throws an error.
     * 
     * @param glOperation
     *            - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
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

    public void addToScene(Element e) {
        mElements.put(e, null);
    }

    public void updateScene(List<Element> elements) {
        mElements.clear();
        for (Element e : elements)
            mElements.put(e, null);
    }

    public void onPause() {
    }

    public void onResume() {
        // Force re-initialisation of static scene objects in this new render thread context
        sAxesDrawable = null;
        mCameraDrawable = null;
    }
    
    private static GL_Primitive cube = new GL_Primitive(new float[] {
            // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.
            
            // Front face
            -0.5f, 0.5f, 0.5f,              
            -0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f, 
            -0.5f, -0.5f, 0.5f,                 
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            
            // Right face
            0.5f, 0.5f, 0.5f,               
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,              
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            
            // Back face
            0.5f, 0.5f, -0.5f,              
            0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,             
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            
            // Left face
            -0.5f, 0.5f, -0.5f,             
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f, 
            -0.5f, -0.5f, -0.5f,                
            -0.5f, -0.5f, 0.5f, 
            -0.5f, 0.5f, 0.5f, 
            
            // Top face
            -0.5f, 0.5f, -0.5f,             
            -0.5f, 0.5f, 0.5f, 
            0.5f, 0.5f, -0.5f, 
            -0.5f, 0.5f, 0.5f,              
            0.5f, 0.5f, 0.5f, 
            0.5f, 0.5f, -0.5f,
            
            // Bottom face
            0.5f, -0.5f, -0.5f,             
            0.5f, -0.5f, 0.5f, 
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, 0.5f,              
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, -0.5f,
    }, Colour.RED.toArray(), Primitive.Type.GL_TRIANGLES.getGLPrimitive());

}
