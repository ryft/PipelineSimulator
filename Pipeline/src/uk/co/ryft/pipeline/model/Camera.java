package uk.co.ryft.pipeline.model;

import java.io.Serializable;

import uk.co.ryft.pipeline.gl.Float3;
import android.opengl.Matrix;
import android.os.SystemClock;

//XXX Virtual camera parameters including camera location and projection
// Necessary because android.graphics.camera doesn't hold projection parameters
public class Camera implements Serializable, Cloneable {

    private static final long serialVersionUID = -2169762992472303103L;
    
    // View parameters
    private Float3 mEye;
    private Float3 mFocus;
    private Float3 mUp;
    
    private Transformation<Camera> transformation;
    
    public Camera(Float3 eye, Float3 focus, Float3 up, float left, float right, float bottom, float top, float near, float far) {
        mEye = (Float3) eye.clone();
        mFocus = (Float3) focus.clone();
        mUp = (Float3) up.clone();
        setProjection(left, right, bottom, top, near, far);
    }

    public void transformTo(Camera destination) {
        transformation = new CameraTransformation(this, destination);
    }

    public Float3 getEye() { return (Float3) mEye.clone(); }
    public Float3 getFocus() { return (Float3) mFocus.clone(); }
    public Float3 getUp() { return (Float3) mUp.clone(); }
    
    private float mScaleFactor = 1;

    public void resetScaleFactor() {
        mScaleFactor = 1;
    }

    public void updateScaleFactor(float scaleFactor) {
        // XXX Zooming implemented as described here: http://www.opengl.org/archives/resources/faq/technical/viewing.htm#view0040
        mScaleFactor /= scaleFactor;
    }
    
    public void setViewMatrix(float[] viewMatrix, int offset) {
        
        Camera cam = (transformation == null) ? this : transformation.getTransformation(SystemClock.uptimeMillis());
        
        Matrix.setLookAtM(viewMatrix, offset,
                cam.getEye().getX(), cam.getEye().getY(), cam.getEye().getZ(),
                cam.getFocus().getX(), cam.getFocus().getY(), cam.getFocus().getZ(),
                cam.getUp().getX(), cam.getUp().getY(), cam.getUp().getZ());
    }

    // Projection parameters
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
    
    @Override
    public Object clone() {
        return new Camera(getEye(), getFocus(), getUp(), getLeft(), getRight(), getBottom(), getTop(), getNear(), getFar());
    }
    
}