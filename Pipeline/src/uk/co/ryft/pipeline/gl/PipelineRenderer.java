package uk.co.ryft.pipeline.gl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.ryft.pipeline.gl.lighting.LightingModel;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Transformation;
import uk.co.ryft.pipeline.model.shapes.Composite;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class PipelineRenderer implements Renderer, Serializable {

    private static final long serialVersionUID = -5651858198215667027L;
    private static final String TAG = "PipelineRenderer";

    // Renderer helper objects passed from the parent
    private final ArrayList<Element> mElements = new ArrayList<Element>();
    private final Map<Element, Drawable> mSceneElements = new ConcurrentHashMap<Element, Drawable>();
    private LightingModel mLighting;

    private final Camera mSceneCamera;
    private final Camera mActualCamera;
    private final Camera mVirtualCamera;

    private boolean mGLCullingEnabled = false;
    private boolean mGLCullingClockwise;
    private boolean mGLDepthBufferEnabled = false;
    private boolean mGLBlendingEnabled = false;
    private boolean mDrawAxes = true;

    // OpenGL matrices stored in float arrays (column-major order)
    private final float[] mModelMatrix = new float[16];
    private final float[] mCameraModelMatrix = new float[16];
    private final float[] mLightModelMatrix = new float[16];

    private final float[] mViewMatrix = new float[16];
    private final float[] mCameraViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private final float[] mMVMatrix = new float[16];
    private final float[] mCVMatrix = new float[16];
    private final float[] mLVMatrix = new float[16];

    private final float[] mMVPMatrix = new float[16];
    private final float[] mCVPMatrix = new float[16];
    private final float[] mLVPMatrix = new float[16];

    // List of model transformations
    // FIXME remove all accesses from outside the render thread
    private final List<Transformation<float[]>> mModelTransformations = new LinkedList<Transformation<float[]>>();

    // Light position, for implementing lighting models
    public static Float3 sLightPosition = new Float3(-1, 1, -2);
    private static Primitive sLightPoint = new Primitive(Primitive.Type.GL_POINTS, Collections.singletonList(sLightPosition),
            Colour.WHITE);
    private Drawable sLightDrawable;

    public float getRotation() {
        return mActualCamera.getRotation();
    }

    public void setRotation(float angle) {
        if (mPipelineState < STEP_CLIPPING)
            mActualCamera.setRotation(angle);
    }

    public void setScaleFactor(float scaleFactor) {
        if (mPipelineState < STEP_CLIPPING) {
            mActualCamera.updateScaleFactor(scaleFactor);
            // Force update to projection matrix
            mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, mSurfaceWidth, mSurfaceHeight);
        }
    }

    public void interact() {
//        mActualCamera.transformTo(mVirtualCamera);
        Log.d(TAG, mActualCamera.toString());
    }

    // Drawables aren't initialised, and are constructed at render time if necessary
    private final Element mCameraElement;
    private Drawable mCameraDrawable;
    private final Element mFrustumElement;
    private Drawable mFrustumDrawable;

    // Axes should never change between instances so they can be declared statically
    private static Composite sAxes;
    private static Drawable sAxesDrawable;

    public PipelineRenderer(Bundle params) {
        
        sAxes = ShapeFactory.buildAxes();

        // Get list of elements from the parameters bundle
        @SuppressWarnings("unchecked")
        ArrayList<Element> elements = (ArrayList<Element>) params.getSerializable("elements");
        mElements.addAll(elements);

        // Initialise cameras
        mSceneCamera = new Camera(new Float3(3, 3, 3), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 2, 8);
        mActualCamera = new Camera(new Float3(3, 3, 3), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 2, 8);
        mVirtualCamera = (Camera) params.getSerializable("camera");

        mCameraElement = ShapeFactory.buildCamera(0.25f);
        mFrustumElement = ShapeFactory.buildFrustum(mVirtualCamera);

        // Initialise lighting model
        mLighting = LightingModel.UNIFORM;

        mGLCullingEnabled = false;
        mGLCullingClockwise = params.getBoolean("culling_clockwise", false);
        mGLDepthBufferEnabled = false;
        mGLBlendingEnabled = false;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);

        // XXX Turn everything off initially
        // TODO Reset state as per mPipelineStep on screen rotation etc

        // Force re-initialisation of static scene objects in this new render thread context
        sAxesDrawable = null;
        sLightDrawable = null;
        mCameraDrawable = null;
        mFrustumDrawable = null;

        LightingModel.resetAll();
        // FIXME For some reason the next call is required. Find out why if there is time (not a priority)
        mLighting.reset();
    }

    int mSurfaceWidth;
    int mSurfaceHeight;

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        mSurfaceWidth = width;
        mSurfaceHeight = height;

        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, width, height);

    }

    Element randomCube = ShapeFactory.buildCuboid(new Float3(0, 0, 0), 1, 1, 1, Colour.RANDOM, Colour.RANDOM);

    protected void setGLParameters() {

        // Set depth buffer parameters
        if (mGLDepthBufferEnabled)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        else
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Set face culling parameters
        if (mGLCullingEnabled)
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        else
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (mGLCullingClockwise)
            GLES20.glFrontFace(GLES20.GL_CW);
        else
            GLES20.glFrontFace(GLES20.GL_CCW);

        // Set blending parameters
        if (mGLBlendingEnabled)
            GLES20.glEnable(GLES20.GL_BLEND);
        else
            GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Clear background colour and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Get the current camera view matrices
        mActualCamera.setViewMatrix(mViewMatrix, 0);
        if (mActualCamera.isTransforming())
            mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, mSurfaceWidth, mSurfaceHeight);
        mVirtualCamera.setViewMatrix(mCameraViewMatrix, 0);

        // Set up the model (world transformation) matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mLightModelMatrix, 0);

        // The camera model matrix transforms the camera to its correct position and orientation in world space
        Matrix.invertM(mCameraModelMatrix, 0, mCameraViewMatrix, 0);

        long time = SystemClock.uptimeMillis();

        // Apply all ongoing transformations to the world, in order, in their current state
        for (Transformation<float[]> t : mModelTransformations)
            // TODO Find a way to remove completed transformations if necessary
            Matrix.multiplyMM(mModelMatrix, 0, t.getTransformation(time), 0, mModelMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mLVMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mCVMatrix, 0, mViewMatrix, 0, mCameraModelMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        Matrix.multiplyMM(mLVPMatrix, 0, mProjectionMatrix, 0, mLVMatrix, 0);
        Matrix.multiplyMM(mCVPMatrix, 0, mProjectionMatrix, 0, mCVMatrix, 0);

        // Initialise axes and camera drawables if necessary
        // Avoid object construction as much as possible at render time
        if (sAxesDrawable == null)
            sAxesDrawable = sAxes.getDrawable();
        if (mCameraDrawable == null)
            mCameraDrawable = mCameraElement.getDrawable();
        if (mFrustumDrawable == null)
            mFrustumDrawable = mFrustumElement.getDrawable();

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Draw axes and virtual camera
        if (mDrawAxes)
            sAxesDrawable.draw(mLighting, mMVMatrix, mMVPMatrix);
        mCameraDrawable.draw(mLighting, mCVMatrix, mCVPMatrix);
        mFrustumDrawable.draw(mLighting, mCVMatrix, mCVPMatrix);

        setGLParameters();

        // Draw world objects in the scene
        for (Element e : mSceneElements.keySet()) {
            if (mSceneElements.get(e) == null)
                mSceneElements.put(e, e.getDrawable());
            Drawable d = mSceneElements.get(e);
            if (d != null)
                d.draw(mLighting, mMVMatrix, mMVPMatrix);
            else
                // Occasionally happens when app is quitting
                // TODO: Investigate turning off continuous rendering when quitting
                System.out.println("Ruh-roh, null drawable!");
        }

        if (sLightDrawable == null)
            sLightDrawable = sLightPoint.getDrawable();
        sLightDrawable.draw(LightingModel.POINT_SOURCE, mLVMatrix, mLVPMatrix);

    }

    // XXX Prints matrices in OpenGL-style column-major order.
    public static void printMatrix(float[] m, int cols, int rows) {
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

    // State is set to the previously-completed pipeline step transition.
    private int mPipelineState = STEP_INITIAL;
    public static final int STEP_INITIAL = 0;
    public static final int STEP_VERTEX_ASSEMBLY = 1; // Add one element at a time
    public static final int STEP_VERTEX_SHADING = 2; // Apply shader gradually?
    public static final int STEP_CLIPPING = 4; // Zoom to virtual camera
    public static final int STEP_MULTISAMPLING = 5; // XX
    public static final int STEP_FACE_CULLING = 6; // ?
    public static final int STEP_FRAGMENT_SHADING = 7; // ?
    public static final int STEP_DEPTH_BUFFER = 8; // ?
    public static final int STEP_BLENDING = 9; // XX
    public static final int STEP_FINAL = STEP_BLENDING;

    class TransitionAnimator extends Thread {

        @Override
        public void run() {
            animationLock = true;
            try {
                switch (mStep) {

                    case STEP_VERTEX_ASSEMBLY:
                        animateVertexAssembly(mForward);
                        break;

                    case STEP_VERTEX_SHADING:
                        animateVertexShading(mForward);
                        break;

                    case STEP_CLIPPING:
                        animateClipping(mForward);
                        break;

                    case STEP_MULTISAMPLING:
                        animateMultisampling(mForward);
                        break;

                    case STEP_FACE_CULLING:
                        animateFaceCulling(mForward);
                        break;

                    case STEP_FRAGMENT_SHADING:
                        animateFragmentShading(mForward);
                        break;

                    case STEP_DEPTH_BUFFER:
                        animateDepthBuffer(mForward);
                        break;

                    case STEP_BLENDING:
                        animateBlending(mForward);
                        break;

                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            animationLock = false;
        }

        private int mStep;
        private boolean mForward;

        // XXX (current step, true)
        public TransitionAnimator(int step, boolean forward) {
            mStep = (forward) ? step + 1 : step;
            mForward = forward;
        }

    }

    // TODO Decide if implementing a monitor for this is worthwhile
    volatile boolean animationLock = false;

    private void animateVertexAssembly(boolean forward) throws InterruptedException {

        String message;
        if (forward) {
            int vertexCount = 0;
            for (Element e : mElements)
                vertexCount += e.getVertexCount();
            message = String.valueOf(vertexCount);
            if (vertexCount == 1)
                message += " vertex assebled";
            else
                message += " vertices assembled";
        } else
            message = "Scene cleared";
        Log.d(TAG, message);

        int interval = (int) (1000.0 / mElements.size()); // Allow 1 second for the whole scene
        Iterable<Element> elements = (forward) ? mElements : mSceneElements.keySet();
        for (Element e : elements) {
            if (forward)
                mSceneElements.put(e, e.getDrawable());
            else
                mSceneElements.remove(e);
            Thread.sleep(interval);
        }
    }

    private void animateVertexShading(boolean forward) throws InterruptedException {
        for (float i = 1; i >= 0; i -= 0.01) {
            mLighting.setGlobalLightLevel(i);
            Thread.sleep(5);
        }
        mLighting = (forward) ? LightingModel.LAMBERTIAN : LightingModel.UNIFORM;
        for (float i = 0; i <= 1; i += 0.01) {
            mLighting.setGlobalLightLevel(i);
            Thread.sleep(5);
        }
    }

    private void animateClipping(boolean forward) throws InterruptedException {
        mDrawAxes = !forward;
        if (forward)
            mActualCamera.transformTo(mVirtualCamera);
        else
            mActualCamera.transformTo(mSceneCamera);
    }

    private void animateMultisampling(boolean forward) throws InterruptedException {

    }

    private void animateFaceCulling(boolean forward) throws InterruptedException {
        mGLCullingEnabled = forward;
    }

    private void animateFragmentShading(boolean forward) throws InterruptedException {
        for (float i = 1; i >= 0; i -= 0.01) {
            mLighting.setGlobalLightLevel(i);
            Thread.sleep(5);
        }
        mLighting = (forward) ? LightingModel.PHONG : LightingModel.LAMBERTIAN;
        for (float i = 0; i <= 1; i += 0.01) {
            mLighting.setGlobalLightLevel(i);
            Thread.sleep(5);
        }
    }

    private void animateDepthBuffer(boolean forward) throws InterruptedException {
        mGLDepthBufferEnabled = forward;
    }

    private void animateBlending(boolean forward) throws InterruptedException {
        mGLBlendingEnabled = forward;
    }

    public void next() {
        if (mPipelineState < STEP_FINAL && !animationLock && !mActualCamera.isTransforming()) {

            new TransitionAnimator(mPipelineState, true).start();
            mPipelineState++;
            Log.d(TAG, "Step " + mPipelineState);

        }
    }

    public void previous() {
        if (mPipelineState > STEP_INITIAL && !animationLock && !mActualCamera.isTransforming()) {

            new TransitionAnimator(mPipelineState, false).start();
            mPipelineState--;
            Log.d(TAG, "Step " + mPipelineState);

        }
    }
}
