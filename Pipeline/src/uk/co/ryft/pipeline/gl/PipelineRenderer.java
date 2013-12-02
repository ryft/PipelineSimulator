package uk.co.ryft.pipeline.gl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Transformation;
import uk.co.ryft.pipeline.model.shapes.Composite;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.model.shapes.Primitive.Type;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class PipelineRenderer implements Renderer {

    private static final String TAG = "PipelineRenderer";

    // OpenGL matrices stored in float arrays (column-major order)
    private final float[] mIdentityMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];
    private final float[] mVPMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private final Map<Element, Drawable> mElements = new LinkedHashMap<Element, Drawable>();

    private final List<Transformation> mModelTransformations = new LinkedList<Transformation>();
    
    Camera camera = new Camera(new FloatPoint(2, 2, 2), new FloatPoint(0, 0, 0), new FloatPoint(0, 1, 0));
    Camera virtualCamera = new Camera(new FloatPoint(-2, 1, 1), new FloatPoint(0, 0, 1), new FloatPoint(0, 1, 0));

    // For touch events
    // TODO: Implement synchronised block for this.
    public volatile float mAngle;
    private final float[] mRotationMatrix = new float[16];
    
    private static Composite axesPrim;
    static {
        LinkedList<Element> prims = new LinkedList<Element>();

        LinkedList<FloatPoint> lineCoords = new LinkedList<FloatPoint>();
        // XXX i < 1.1 is required to draw the edge lines
        for (float i = -1; i < 1.1; i += 0.1) {
            lineCoords.add(new FloatPoint(i, 0, -1));
            lineCoords.add(new FloatPoint(i, 0, 1));
            lineCoords.add(new FloatPoint(-1, 0, i));
            lineCoords.add(new FloatPoint(1, 0, i));
        }
        prims.add(new Primitive(Type.GL_LINES, lineCoords, Colour.GREY));
        
        LinkedList<FloatPoint> points = new LinkedList<FloatPoint>();
        points.add(new FloatPoint(0, 0, 0));
        points.add(new FloatPoint(1, 0, 0));
        points.add(new FloatPoint(0, 0, 0));
        points.add(new FloatPoint(0, 1, 0));
        points.add(new FloatPoint(0, 0, 0));
        points.add(new FloatPoint(0, 0, 1));
        prims.add(new Primitive(Type.GL_LINES, points, Colour.WHITE));

        LinkedList<FloatPoint> arrX = new LinkedList<FloatPoint>();
        arrX.add(new FloatPoint(0.8f, 0.1f, -0.1f));
        arrX.add(new FloatPoint(1, 0, 0));
        arrX.add(new FloatPoint(0.8f, -0.1f, 0.1f));
        arrX.add(new FloatPoint(0.9f, 0, 0));
        prims.add(new Primitive(Type.GL_LINE_LOOP, arrX, Colour.RED));

        LinkedList<FloatPoint> arrY = new LinkedList<FloatPoint>();
        arrY.add(new FloatPoint(-0.1f, 0.8f, 0.1f));
        arrY.add(new FloatPoint(0, 1, 0));
        arrY.add(new FloatPoint(0.1f, 0.8f, -0.1f));
        arrY.add(new FloatPoint(0, 0.9f, 0));
        prims.add(new Primitive(Type.GL_LINE_LOOP, arrY, Colour.GREEN));

        LinkedList<FloatPoint> arrZ = new LinkedList<FloatPoint>();
        arrZ.add(new FloatPoint(0.1f, -0.1f, 0.8f));
        arrZ.add(new FloatPoint(0, 0, 1));
        arrZ.add(new FloatPoint(-0.1f, 0.1f, 0.8f));
        arrZ.add(new FloatPoint(0, 0, 0.9f));
        prims.add(new Primitive(Type.GL_LINE_LOOP, arrZ, Colour.CYAN));
        
        axesPrim = new Composite(Composite.Type.CUSTOM_SHAPE, prims);
    }

    public void interact() {
//        mModelTransformations.add(new Translation(new FloatPoint(0, 1, 0), 100));
//        mModelTransformations.add(new Rotation(180, new FloatPoint(0, 0, 1), 100));
//        mCameraEyeTransformations.add(new Translation(new FloatPoint(0, 0.5f, 0), 100));
        camera.transformTo(virtualCamera, 100);
    }

    // TODO Should these belong here?
    public static final String VERTEX_SHADER_EMPTY =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // the order must be matrix * vector as the matrix is in col-major order.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    public static final String FRAGMENT_SHADER_EMPTY =
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

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
        Matrix.setIdentityM(mRotationMatrix, 0);
    }

    // XXX Virtual camera parameters
    class Camera {

        private FloatPoint mEye;
        private FloatPoint mFocus;
        private FloatPoint mUp;

        private FloatPoint mEyeTarget;
        private FloatPoint mFocusTarget;
        private FloatPoint mUpTarget;
        
        private FloatPoint mEyeStep;
        private FloatPoint mFocusStep;
        private FloatPoint mUpStep;
        
        private int mSteps = 0;
        private int mStep = 0;
        
        private float[] mPrevView;
        
        public Camera(FloatPoint eye, FloatPoint focus, FloatPoint up) {
            mEye = (FloatPoint) eye.clone();
            mFocus = (FloatPoint) focus.clone();
            mUp = (FloatPoint) up.clone();
        }

        public void transformTo(Camera camera, int steps) {
            transformTo(camera.mEye, camera.mFocus, camera.mUp, steps);
        }

        public void transformTo(FloatPoint eye, FloatPoint focus, FloatPoint up, int steps) {
            
            mSteps = steps;
            
            mEyeTarget = (FloatPoint) eye.clone();
            mFocusTarget = (FloatPoint) focus.clone();
            mUpTarget = (FloatPoint) up.clone();

            FloatPoint eyeDiff = mEyeTarget.minus(mEye);
            FloatPoint focusDiff = mFocusTarget.minus(mFocus);
            FloatPoint upDiff = mUpTarget.minus(mUp);
            
            mEyeStep = eyeDiff.scale(1.0f / mSteps);
            mFocusStep = focusDiff.scale(1.0f / mSteps);
            mUpStep = upDiff.scale(1.0f / mSteps);
            
            // Invalidate previous completed view matrix
            mPrevView = null;
            mStep = 0;
        }
        
        public float[] getViewMatrix() {
            
            if (mStep < mSteps) {
                mEye = mEye.plus(mEyeStep);
                mFocus = mFocus.plus(mFocusStep);
                mUp = mUp.plus(mUpStep);

                if (mPrevView == null)
                    mPrevView = new float[16];
                
                Matrix.setLookAtM(mPrevView, 0, mEye.getX(), mEye.getY(), mEye.getZ(), mFocus.getX(), mFocus.getY(), mFocus.getY(), mUp.getX(), mUp.getY(), mUp.getZ());
                mStep++;
                
            } else if (mPrevView == null) {
                mPrevView = new float[16];
                Matrix.setLookAtM(mPrevView, 0, mEye.getX(), mEye.getY(), mEye.getZ(), mFocus.getX(), mFocus.getY(), mFocus.getY(), mUp.getX(), mUp.getY(), mUp.getZ());
                
            } // Otherwise the animation has completed and we can reuse the last view matrix
            
            return mPrevView;
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Clear background colour and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set up an identity matrix
        Matrix.setIdentityM(mIdentityMatrix, 0);

        // Set up the model (world transformation) matrix
        Matrix.setIdentityM(mModelMatrix, 0);

        float[] viewMatrix = camera.getViewMatrix();
        // Params: matrix, offset, eye(x, y, z), focus(x, y, z), up(x, y, z).
        // XXX Coords are flipped on screen with frustrumM a la http://www.learnopengles.com/understanding-opengls-matrices/
        
        // Get the current camera view matrix
        float[] cameraViewMatrix = virtualCamera.getViewMatrix();
        // The camera model matrix transforms camera to its correct position and orientation in world space
        float[] cameraModelMatrix = new float[16];
        Matrix.invertM(cameraModelMatrix, 0, cameraViewMatrix, 0);
        
        // Transform objects in the scene from world coords to camera coords
        Matrix.multiplyMM(mModelMatrix, 0, cameraModelMatrix, 0, mModelMatrix, 0);

        // Apply all transformations to the world, in order, in their current state
        for (Transformation t : mModelTransformations)
            Matrix.multiplyMM(mModelMatrix, 0, t.next(), 0, mModelMatrix, 0);
        
        // Combine the rotation matrix with the projection and camera view
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 1, 0);
        Matrix.multiplyMM(mModelMatrix, 0, mRotationMatrix, 0, mModelMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mMVMatrix, 0, viewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);

        // Ignore world (model) coord transformation when drawing axes
        axesPrim.getDrawable().draw(mVPMatrix);
        
        // Draw objects in the scene
        for (Element e : mElements.keySet()) {
            if (mElements.get(e) == null)
                mElements.put(e, e.getDrawable());
            Drawable d = mElements.get(e);
            if (d != null)
                d.draw(mMVPMatrix);
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

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        // XXX display a unit square with correct aspect ratio, regardless of screen orientation
        if (width >= height) {
            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 7);
            // (float[] m, int offset, float left, float right, float bottom, float top, float near, float far)

        } else {
            float ratio = (float) height / width;
            Matrix.frustumM(mProjMatrix, 0, -1, 1, -ratio, ratio, 1, 7);
//            float ratio = (float) width / height;
//            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        }

    }

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
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

    public void addToScene(Element e) {
        mElements.put(e, null);
    }

    public void updateScene(List<Element> elements) {
        mElements.clear();
        for (Element e : elements)
            mElements.put(e, null);
    }

}
