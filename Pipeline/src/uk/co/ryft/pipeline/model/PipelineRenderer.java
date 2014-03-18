package uk.co.ryft.pipeline.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.ryft.pipeline.SetupActivity;
import uk.co.ryft.pipeline.model.camera.Camera;
import uk.co.ryft.pipeline.model.element.Composite;
import uk.co.ryft.pipeline.model.element.Element;
import uk.co.ryft.pipeline.model.element.Primitive;
import uk.co.ryft.pipeline.model.element.ShapeFactory;
import uk.co.ryft.pipeline.model.lighting.LightingModel;
import uk.co.ryft.pipeline.model.lighting.LightingModel.Model;
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
    private LightingModel mAccessoryLighting;
    private LightingModel mPointLighting;

    private final Camera mSceneCamera;
    private final Camera mActualCamera;
    private final Camera mVirtualCamera;

    private boolean mGLCullingEnabled = false;
    private boolean mGLCullingClockwise;

    private boolean mGLDepthBufferEnabled = false;
    private int mGLDepthFunc = 1;

    private boolean mGLBlendEnabled = false;
    private int mGLBlendFuncSrc = 2;
    private int mGLBlendFuncDst = 3;
    private int mGLBlendEquation = 0;

    private boolean mDrawAxes = true;
    private boolean mDrawCamera = true;
    private boolean mDrawFrustum = true;

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

    protected int mAnimationDuration = 2000;

    // Light position, for implementing lighting models
    public static Float3 sLightPosition;
    private Primitive mLightElement;
    private Drawable mLightDrawable;

    public float getRotation() {
        return mActualCamera.getRotation();
    }

    public void setRotation(float angle) {
        if (mPipelineState < STEP_CLIPPING)
            mActualCamera.setRotation(angle);
    }

    public float getScaleFactor() {
        return mActualCamera.getScaleFactor();
    }

    public void setScaleFactor(float scaleFactor) {
        mActualCamera.setScaleFactor(scaleFactor);
        // Force update to projection matrix
        mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, mSurfaceWidth, mSurfaceHeight);
    }

    public void updateScaleFactor(float scaleFactor) {
        if (mPipelineState < STEP_CLIPPING) {
            mActualCamera.updateScaleFactor(scaleFactor);
            // Force update to projection matrix
            mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, mSurfaceWidth, mSurfaceHeight);
        }
    }

    public void setGlobalLightLevel(float alpha) {
        mLighting.setGlobalLightLevel(alpha);
    }

    public void interact() {
        // mActualCamera.transformTo(mVirtualCamera);
        Log.d(TAG, mActualCamera.toString());
    }

    // Drawables aren't initialised, and are constructed at render time if necessary
    private final Element mCameraElement;
    private Drawable mCameraDrawable;
    private final Element mFrustumElement;
    private Drawable mFrustumDrawable;

    private Composite sAxes;
    private Drawable sAxesDrawable;

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

        // Initialise lighting models
        mLighting = LightingModel.getLightingModel(Model.UNIFORM);
        mAccessoryLighting = LightingModel.getLightingModel(Model.UNIFORM);
        mPointLighting = LightingModel.getLightingModel(Model.POINT_SOURCE);

        sLightPosition = (Float3) params.getSerializable("light_position");
        mLightElement = new Primitive(Primitive.Type.GL_POINTS, Collections.singletonList(sLightPosition), Colour.WHITE);
        mLightDrawable = mLightElement.getDrawable();

        mGLCullingEnabled = false;
        mGLCullingClockwise = params.getBoolean("culling_clockwise", false);

        mGLDepthBufferEnabled = false;
        mGLDepthFunc = params.getInt("depth_func", mGLDepthFunc);

        mGLBlendEnabled = false;
        mGLBlendFuncSrc = params.getInt("blend_func_src", mGLBlendFuncSrc);
        mGLBlendFuncDst = params.getInt("blend_func_dst", mGLBlendFuncDst);
        mGLBlendEquation = params.getInt("blend_equation", mGLBlendEquation);

        mAnimationDuration = params.getInt("animation_duration", mAnimationDuration);

        GLES20.glCullFace(GLES20.GL_BACK);
        if (mGLCullingClockwise)
            GLES20.glFrontFace(GLES20.GL_CW);
        else
            GLES20.glFrontFace(GLES20.GL_CCW);

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearDepthf(1.0f);

        // XXX Turn everything off initially
        // TODO Reset state as per mPipelineStep on screen rotation etc

        // Force re-initialisation of static scene objects in this new render thread context
        sAxesDrawable = null;
        mLightDrawable = null;
        mCameraDrawable = null;
        mFrustumDrawable = null;

        mLighting.reset();
        mAccessoryLighting.reset();
        mPointLighting.reset();
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

    protected void setEmptyGLParameters() {

        // Reset selected scene GL parameters for drawing scene accessories
        // e.g. axes, virtual camera model
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    protected boolean mAnimatingCulling = false;
    protected boolean mAnimatingDepthBuffer = false;
    protected boolean mAnimatingBlending = false;

    protected int mAnimatedElements = 0;
    protected boolean mAnimationForward;

    protected void setGLParameters(int drawn) {

        // Set face culling parameters
        boolean cullFace = mGLCullingEnabled;
        if (mAnimatingCulling && drawn < mAnimatedElements)
            cullFace = mAnimationForward;

        if (cullFace)
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        else
            GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Set depth buffer parameters
        boolean depthBuffer = mGLDepthBufferEnabled;
        if (mAnimatingDepthBuffer && drawn < mAnimatedElements)
            depthBuffer = mAnimationForward;

        if (depthBuffer) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthFunc(SetupActivity.DepthFunc.mValues[mGLDepthFunc]);
        } else
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Set blending parameters
        boolean blending = mGLBlendEnabled;
        if (mAnimatingBlending && drawn < mAnimatedElements)
            blending = mAnimationForward;

        if (blending) {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(SetupActivity.BlendFunc.mValues[mGLBlendFuncSrc],
                    SetupActivity.BlendFunc.mValues[mGLBlendFuncDst]);
            GLES20.glBlendEquation(SetupActivity.BlendEquation.mValues[mGLBlendEquation]);
        } else
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

        setEmptyGLParameters();

        // Draw axes and virtual camera
        if (mDrawAxes)
            sAxesDrawable.draw(mAccessoryLighting, mMVMatrix, mMVPMatrix);
        if (mDrawCamera)
            mCameraDrawable.draw(mAccessoryLighting, mCVMatrix, mCVPMatrix);
        if (mDrawFrustum)
            mFrustumDrawable.draw(mAccessoryLighting, mCVMatrix, mCVPMatrix);

        int drawn = 0;

        // Draw world objects in the scene
        for (Element e : mSceneElements.keySet()) {
            if (mSceneElements.get(e) == null)
                mSceneElements.put(e, e.getDrawable());
            Drawable d = mSceneElements.get(e);

            setGLParameters(drawn);
            if (d != null) {
                d.draw(mLighting, mMVMatrix, mMVPMatrix);
                drawn++;
            } else
                // Occasionally happens when app is quitting
                // TODO: Investigate turning off continuous rendering when quitting
                System.out.println("Ruh-roh, null drawable!");
        }

        if (mLightDrawable == null)
            mLightDrawable = mLightElement.getDrawable();
        mLightDrawable.draw(mPointLighting, mLVMatrix, mLVPMatrix);

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
    public static final int STEP_CLIPPING = 3; // Zoom to virtual camera
    public static final int STEP_MULTISAMPLING = 4; // XX
    public static final int STEP_FACE_CULLING = 5; // ?
    public static final int STEP_FRAGMENT_SHADING = 6; // ?
    public static final int STEP_DEPTH_BUFFER = 7; // ?
    public static final int STEP_BLENDING = 8; // XX
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
    boolean tempAnimatingCulling = false;

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

        int interval = (int) ((double) mAnimationDuration / mElements.size());
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

        // Calculate number of steps from (duration = #steps * interval)
        // Interval is fixed length (10ms)
        int steps = mAnimationDuration / 20;

        for (int step = 0; step <= steps; step++) {
            mLighting.setGlobalLightLevel(1 - ((float) step / steps));
            Thread.sleep(10);
        }
        mLighting = (forward) ? LightingModel.getLightingModel(Model.LAMBERTIAN) : LightingModel
                .getLightingModel(Model.UNIFORM);
        for (int step = 0; step <= steps; step++) {
            mLighting.setGlobalLightLevel((float) step / steps);
            Thread.sleep(10);
        }
    }

    private void animateClipping(boolean forward) throws InterruptedException {
        mDrawAxes = !forward;
        if (forward)
            mActualCamera.transformTo(mVirtualCamera, mAnimationDuration);
        else
            mActualCamera.transformTo(mSceneCamera, mAnimationDuration);
    }

    private void animateMultisampling(boolean forward) throws InterruptedException {
        // This animation is performed by the enclosing activity as we need to swap out the surface
    }

    private void animateFaceCulling(boolean forward) throws InterruptedException {

        mAnimatingCulling = true;
        mAnimationForward = forward;

        int interval = (int) ((double) mAnimationDuration / mElements.size());
        animate(interval);

        mAnimatingCulling = false;
        mGLCullingEnabled = forward;
    }

    private void animateFragmentShading(boolean forward) throws InterruptedException {
        int steps = mAnimationDuration / 20;
        for (int step = 0; step <= steps; step++) {
            mLighting.setGlobalLightLevel(1 - ((float) step / steps));
            Thread.sleep(10);
        }
        mLighting = (forward) ? LightingModel.getLightingModel(Model.PHONG) : LightingModel.getLightingModel(Model.LAMBERTIAN);
        for (int step = 0; step <= steps; step++) {
            mLighting.setGlobalLightLevel((float) step / steps);
            Thread.sleep(10);
        }
    }

    private void animate(int interval) throws InterruptedException {
        while (mAnimatedElements < mElements.size()) {
            mAnimatedElements++;
            Thread.sleep(interval);
        }
        mAnimatedElements = 0;
    }

    private void animateDepthBuffer(boolean forward) throws InterruptedException {

        mAnimatingDepthBuffer = true;
        mAnimationForward = forward;

        int interval = (int) ((double) mAnimationDuration / mElements.size());
        animate(interval);

        mAnimatingDepthBuffer = false;
        mGLDepthBufferEnabled = forward;
    }

    private void animateBlending(boolean forward) throws InterruptedException {

        mAnimatingBlending = true;
        mAnimationForward = forward;

        int interval = (int) ((double) mAnimationDuration / mElements.size());
        animate(interval);

        mAnimatingBlending = false;
        mGLBlendEnabled = forward;
        mDrawCamera = !forward;
        mDrawFrustum = !forward;
    }

    public void next() {
        if (mPipelineState < STEP_FINAL && !animationLock && !mActualCamera.isTransforming()) {

            new TransitionAnimator(mPipelineState, true).start();
            mPipelineState++;
        }
    }

    public void previous() {
        if (mPipelineState > STEP_INITIAL && !animationLock && !mActualCamera.isTransforming()) {

            new TransitionAnimator(mPipelineState, false).start();
            mPipelineState--;
        }
    }

    public int getCurrentState() {
        return mPipelineState;
    }

    public String getNextStepDescription() {
        return getStepDescription(mPipelineState + 1);
    }

    public String getPrevStepDescription() {
        return getStepDescription(mPipelineState);
    }

    private String getStepDescription(int step) {
        switch (step) {
            case STEP_INITIAL:
                return "Empty Scene";
            case STEP_VERTEX_ASSEMBLY:
                return "Vertex Assembly";
            case STEP_VERTEX_SHADING:
                return "Vertex Shading";
            case STEP_CLIPPING:
                return "Viewport Mapping and Clipping";
            case STEP_MULTISAMPLING:
                return "Multisampling";
            case STEP_FACE_CULLING:
                return "Back Face Culling";
            case STEP_FRAGMENT_SHADING:
                return "Fragment Shading";
            case STEP_DEPTH_BUFFER:
                return "Depth Buffer Test";
            case STEP_BLENDING:
                return "Blending";
            case STEP_FINAL + 1:
                return "Render Complete";
            default:
                return "Undefined Step";
        }
    }
}
