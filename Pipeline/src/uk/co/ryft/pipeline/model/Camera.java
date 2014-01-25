package uk.co.ryft.pipeline.model;

import java.io.Serializable;

import uk.co.ryft.pipeline.gl.Float3;
import android.opengl.Matrix;

//XXX Virtual camera parameters including camera location and projection
// Necessary because android.graphics.camera doesn't hold projection parameters
public class Camera implements Serializable {

    private static final long serialVersionUID = -2169762992472303103L;
    
    // View parameters
    private Float3 mEye;
    private Float3 mFocus;
    private Float3 mUp;

    // Projection parameters
//    private float 
    
    
//    // Transition view parameters
//    private FloatPoint mEyeTarget;
//    private FloatPoint mFocusTarget;
//    private FloatPoint mUpTarget;
//    
//    private FloatPoint mEyeStep;
//    private FloatPoint mFocusStep;
//    private FloatPoint mUpStep;
//    
//    private int mSteps = 0;
//    private int mStep = 0;
//    
//    private float[] mPrevView;
    
    public Camera(Float3 eye, Float3 focus, Float3 up, float left, float right, float bottom, float top, float near, float far) {
        mEye = (Float3) eye.clone();
        mFocus = (Float3) focus.clone();
        mUp = (Float3) up.clone();
        setProjection(left, right, bottom, top, near, far);
    }

//    public void transformTo(Camera camera, int steps) {
//        transformTo(camera.mEye, camera.mFocus, camera.mUp, steps);
//    }
//
//    public void transformTo(FloatPoint eye, FloatPoint focus, FloatPoint up, int steps) {
//        
//        mSteps = steps;
//        
//        mEyeTarget = (FloatPoint) eye.clone();
//        mFocusTarget = (FloatPoint) focus.clone();
//        mUpTarget = (FloatPoint) up.clone();
//
//        FloatPoint eyeDiff = mEyeTarget.minus(mEye);
//        FloatPoint focusDiff = mFocusTarget.minus(mFocus);
//        FloatPoint upDiff = mUpTarget.minus(mUp);
//        
//        mEyeStep = eyeDiff.scale(1.0f / mSteps);
//        mFocusStep = focusDiff.scale(1.0f / mSteps);
//        mUpStep = upDiff.scale(1.0f / mSteps);
//        
//        // Invalidate previous completed view matrix
//        mPrevView = null;
//        mStep = 0;
//    }

    public Float3 getEye() { return (Float3) mEye.clone(); }
    public Float3 getFocus() { return (Float3) mFocus.clone(); }
    public Float3 getUp() { return (Float3) mUp.clone(); }
    
    private float mScaleFactor = 1;

    public void setScaleFactor(float scaleFactor) {
        // XXX Zooming implemented as described here: http://www.opengl.org/archives/resources/faq/technical/viewing.htm#view0040
        mScaleFactor /= scaleFactor;
    }
    
    public void setViewMatrix(float[] viewMatrix, int offset) {
        
//        if (mStep < mSteps) {
//            mEye = mEye.plus(mEyeStep);
//            mFocus = mFocus.plus(mFocusStep);
//            mUp = mUp.plus(mUpStep);
//
//            if (mPrevView == null)
//                mPrevView = new float[16];
            
        Matrix.setLookAtM(viewMatrix, offset, mEye.getX(), mEye.getY(), mEye.getZ(), mFocus.getX(), mFocus.getY(), mFocus.getY(), mUp.getX(), mUp.getY(), mUp.getZ());
//            mStep++;
//            
//        } else if (mPrevView == null) {
//            mPrevView = new float[16];
//            Matrix.setLookAtM(mPrevView, 0, mEye.getX(), mEye.getY(), mEye.getZ(), mFocus.getX(), mFocus.getY(), mFocus.getY(), mUp.getX(), mUp.getY(), mUp.getZ());
//            
//        } // Otherwise the animation has completed and we can reuse the last view matrix
//        
//        return mPrevView;
    }

    float frustumLeft;
    float frustumRight;
    float frustumBottom;
    float frustumTop;
    float frustumNear;
    float frustumFar;

    public float getLeft() { return frustumLeft; }
    public float getRight() { return frustumRight; }
    public float getBottom() { return frustumBottom; }
    public float getTop() { return frustumTop; }
    public float getNear() { return frustumNear; }
    public float getFar() { return frustumFar; }

    public void setProjection(float left, float right, float bottom, float top, float near, float far) {
        frustumLeft = left;
        frustumRight = right;
        frustumBottom = bottom;
        frustumTop = top;
        frustumNear = near;
        frustumFar = far;
    }

    public void setProjectionMatrix(float[] projectionMatrix, int offset, int width, int height) {

        // XXX display a unit square with correct aspect ratio, regardless of screen orientation
        if (width >= height) {
            float ratio = (float) width / height;
            
            Matrix.frustumM(projectionMatrix, offset, -ratio * mScaleFactor, ratio * mScaleFactor, frustumBottom * mScaleFactor, frustumTop * mScaleFactor, frustumNear, frustumFar);
            // (float[] m, int offset, float left, float right, float bottom, float top, float near, float far)

        } else {
            float ratio = (float) height / width;
            Matrix.frustumM(projectionMatrix, offset, frustumLeft * mScaleFactor, frustumRight * mScaleFactor, -ratio * mScaleFactor, ratio * mScaleFactor, frustumNear, frustumFar);
//            float ratio = (float) width / height;
//            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        }
    }
    
}