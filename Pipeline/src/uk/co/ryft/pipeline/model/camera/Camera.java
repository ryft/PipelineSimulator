package uk.co.ryft.pipeline.model.camera;

import java.io.Serializable;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.Transformation;
import android.opengl.Matrix;
import android.os.SystemClock;

//XXX Virtual camera parameters including camera location and projection
// Necessary because android.graphics.camera doesn't hold projection parameters
public class Camera implements Serializable, Cloneable {

    private static final long serialVersionUID = -2169762992472303103L;
    @SuppressWarnings("unused")
    private static final String TAG = "Camera";
    
    // View parameters
    private Float3 mEye;
    private Float3 mFocus;
    private Float3 mUp;
    
    private Transformation<Camera> mTransformation;
    
    public Camera(Float3 eye, Float3 focus, Float3 up, float left, float right, float bottom, float top, float near, float far) {
        mEye = eye;
        mFocus = focus;
        mUp = up;
        setProjection(left, right, bottom, top, near, far);
    }

    public void transformTo(Camera destination, int duration) {
        mTransformation = new CameraTransformation(this, destination, duration);
    }

    public Float3 getEye() { return mEye; }
    public Float3 getFocus() { return mFocus; }
    public Float3 getUp() { return mUp; }

    // For touch events
    private volatile float mRotation = 0f;
    
    public float getRotation() {
        return mRotation;
    }
    
    public void setRotation(float rotation) {
        mRotation = rotation % 360;
    }
    
    private float mScaleFactor = 1;
    
    public float getScaleFactor() {
        return mScaleFactor;
    }
    
    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    public void updateScaleFactor(float scaleFactor) {
        // Zooming implemented as described here: http://www.opengl.org/archives/resources/faq/technical/viewing.htm#view0040
        mScaleFactor /= scaleFactor;
    }
    
    public void setViewMatrix(float[] viewMatrix, int offset) {
        
        if (mTransformation != null) {
            long time = SystemClock.uptimeMillis();
            Camera newCamera = mTransformation.getTransformation(time);
            mEye = newCamera.getEye();
            mFocus = newCamera.getFocus();
            mUp = newCamera.getUp();
            setProjection(newCamera.getLeft(), newCamera.getRight(), newCamera.getBottom(), newCamera.getTop(), newCamera.getNear(), newCamera.getFar());
            mRotation = newCamera.mRotation;

            if (mTransformation.isComplete(time))
                mTransformation = null;
        }

        // Use negative angle to rotate in the correct direction about the y-axis
        Float3 eye = mEye.rotate(-mRotation, 0, 1, 0);
        
        Matrix.setLookAtM(viewMatrix, offset,
                eye.getX(), eye.getY(), eye.getZ(),
                getFocus().getX(), getFocus().getY(), getFocus().getZ(),
                getUp().getX(), getUp().getY(), getUp().getZ());
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
    
    // Determine whether a projection re-calculation is required at render time
    public boolean isTransforming() {
        
        return (mTransformation != null && !mTransformation.isComplete(SystemClock.uptimeMillis()));
    }

    public void setProjectionMatrix(float[] projectionMatrix, int offset, int width, int height) {
        
        if (mTransformation != null) {
            Camera newCamera = mTransformation.getTransformation(SystemClock.uptimeMillis());
            mScaleFactor = newCamera.mScaleFactor;
        }

        // XXX display a unit square with correct aspect ratio, regardless of screen orientation
        if (width >= height) {
            float ratio = (float) width / height;
            Matrix.frustumM(projectionMatrix, offset, -ratio * mScaleFactor, ratio * mScaleFactor, frustumBottom * mScaleFactor, frustumTop * mScaleFactor, frustumNear, frustumFar);

        } else {
            float ratio = (float) height / width;
            Matrix.frustumM(projectionMatrix, offset, frustumLeft * mScaleFactor, frustumRight * mScaleFactor, -ratio * mScaleFactor, ratio * mScaleFactor, frustumNear, frustumFar);

        }
    }
    
    @Override
    public String toString() {
        StringBuilder summary = new StringBuilder("View Parameters\n");
        summary.append("Eye point: " + getEye() + "\n");
        summary.append("Focus point: " + getFocus() + "\n");
        summary.append("Up direction: " + getUp() + "\n");
        summary.append("\nProjection parameters\n");
        summary.append("Left " + getLeft() + ", Right " + getRight() + ", Bottom " + getBottom() + ", Top " + getTop() + ", Near " + getNear() + ", Far " + getFar() + "\n");
        summary.append("\nDynamic parameters\n");
        summary.append("Rotation " + getRotation() + ", Scale factor " + getScaleFactor());
        return summary.toString();
    }
    
    @Override
    public Camera clone() {
        Camera cloned = new Camera(getEye(), getFocus(), getUp(), getLeft(), getRight(), getBottom(), getTop(), getNear(), getFar());
        cloned.setRotation(getRotation());
        cloned.setScaleFactor(getScaleFactor());
        return cloned;
    }
    
}