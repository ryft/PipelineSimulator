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
    private final Map<Element, Drawable> mElements = new ConcurrentHashMap<Element, Drawable>();
    private LightingModel mLighting;

    private final Camera mActualCamera;
    private final Camera mVirtualCamera;

    // Rendering parameters passed from the parent
    private final boolean mCullingEnabled;
    private final boolean mCullingClockwise;
    private final boolean mDepthBufferEnabled;
    private final boolean mBlendingEnabled;

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

    private final List<Transformation> mModelTransformations = new LinkedList<Transformation>();

    // Light position, for implementing lighting models
    public static Float3 sLightPosition = new Float3(2, 0, 0);
    private static Primitive sLightPoint = new Primitive(Primitive.Type.GL_POINTS, Collections.singletonList(sLightPosition),
            Colour.WHITE);
    private Drawable sLightDrawable;

    // For touch events
    // TODO: Implement a monitor for this.
    private volatile float mAngle;
    private final float[] mModelRotationMatrix = new float[16];

    public float getRotation() {
        return mAngle;
    }

    public void setRotation(float angle) {
        mAngle = angle;
    }

    public void setScaleFactor(float scaleFactor) {
        mActualCamera.setScaleFactor(scaleFactor);
        mActualCamera.setProjectionMatrix(mProjectionMatrix, 0, mSurfaceWidth, mSurfaceHeight);
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
        for (Element e : elements)
            mElements.put(e, e.getDrawable());

        // Initialise cameras
        mActualCamera = new Camera(new Float3(2, 2, 2), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 1.5f, 7.0f);
        mVirtualCamera = (Camera) params.getSerializable("camera");

        mCameraElement = ShapeFactory.buildCamera(0.25f);
        mFrustumElement = ShapeFactory.buildFrustum(mVirtualCamera);

        // Initialise lighting model
        mLighting = (LightingModel) params.getSerializable("lighting");

        mCullingEnabled = params.getBoolean("culling_enabled", true);
        mCullingClockwise = params.getBoolean("culling_clockwise", false);
        mDepthBufferEnabled = params.getBoolean("depth_buffer_enabled", true);
        mBlendingEnabled = params.getBoolean("blending_enabled", true);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);

        // XXX Turn everything off initially
        // TODO Reset state as per mPipelineStep on screen rotation etc
        
        // Set depth buffer parameters
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Set face culling parameters
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (mCullingClockwise)
            GLES20.glFrontFace(GLES20.GL_CW);
        else
            GLES20.glFrontFace(GLES20.GL_CCW);

        // For touch events
        Matrix.setIdentityM(mModelRotationMatrix, 0);

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

    @Override
    public void onDrawFrame(GL10 unused) {

        // Clear background colour and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Get the current camera view matrices
        mActualCamera.setViewMatrix(mViewMatrix, 0);
        mVirtualCamera.setViewMatrix(mCameraViewMatrix, 0);

        // Set up the model (world transformation) matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mLightModelMatrix, 0);

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

        // Draw axes and virtual camera
        sAxesDrawable.draw(mLighting, mMVMatrix, mMVPMatrix);
        mCameraDrawable.draw(mLighting, mCVMatrix, mCVPMatrix);
        mFrustumDrawable.draw(mLighting, mCVMatrix, mCVPMatrix);

        // Draw world objects in the scene
        for (Element e : mElements.keySet()) {
            if (mElements.get(e) == null)
                mElements.put(e, e.getDrawable());
            Drawable d = mElements.get(e);
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

    private int mPipelineStep = STEP_INITIAL;
    public static final int STEP_INITIAL = 0;
    public static final int STEP_VERTEX_ASSEMBLY = 1;
    public static final int STEP_VERTEX_SHADING = 2;
    public static final int STEP_GEOMETRY_SHADING = 3;
    public static final int STEP_CLIPPING = 4;
    public static final int STEP_MULTISAMPLING = 5;
    public static final int STEP_FACE_CULLING = 6;
    public static final int STEP_FRAGMENT_SHADING = 7;
    public static final int STEP_DEPTH_BUFFER = 8;
    public static final int STEP_BLENDING = 9;

    public void next() {
        mPipelineStep++;
        Log.d(TAG, "Step " + mPipelineStep);

        if (mPipelineStep >= STEP_FACE_CULLING)
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        if (mPipelineStep >= STEP_DEPTH_BUFFER)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        onDrawFrame(null);
    }

    public void previous() {
        mPipelineStep--;
        Log.d(TAG, "Step " + mPipelineStep);

        if (mPipelineStep < STEP_FACE_CULLING)
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        if (mPipelineStep < STEP_DEPTH_BUFFER)
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }
}
