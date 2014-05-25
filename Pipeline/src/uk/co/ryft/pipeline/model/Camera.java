package uk.co.ryft.pipeline.model;

import android.opengl.Matrix;
import android.os.SystemClock;

import java.io.Serializable;

import uk.co.ryft.pipeline.model.transformation.CameraTransformation;
import uk.co.ryft.pipeline.model.transformation.Transformation;

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

    public Float3 getEye() {
        return mEye;
    }

    public Float3 getFocus() {
        return mFocus;
    }

    public Float3 getUp() {
        return mUp;
    }

    // For touch events. This is modified by the UI thread so needs to be volatile to ensure a global access ordering.
    private volatile float mRotation = 0f;

    public float getRotation() {
        return mRotation;
    }

    /**
     * Set the scene rotation in degrees, about the y-axis.
     * Thread safe operation, can be called by the UI thread.
     *
     * @param rotation The rotation in degrees.
     */
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
            setProjection(newCamera.getLeft(), newCamera.getRight(), newCamera.getBottom(), newCamera.getTop(),
                    newCamera.getNear(), newCamera.getFar());
            mRotation = newCamera.mRotation;
        }

        // Use negative angle to rotate in the correct direction about the y-axis
        Float3 eye = mEye.rotate(-mRotation, 0, 1, 0);

        Matrix.setLookAtM(viewMatrix, offset, eye.getX(), eye.getY(), eye.getZ(), getFocus().getX(), getFocus().getY(),
                getFocus().getZ(), getUp().getX(), getUp().getY(), getUp().getZ());
    }

    // Projection parameters
    private float mFrustumLeft;
    private float mFrustumRight;
    private float mFrustumBottom;
    private float mFrustumTop;
    private float mFrustumNear;
    private float mFrustumFar;

    public float getLeft() {
        return mFrustumLeft;
    }

    public float getRight() {
        return mFrustumRight;
    }

    public float getBottom() {
        return mFrustumBottom;
    }

    public float getTop() {
        return mFrustumTop;
    }

    public float getNear() {
        return mFrustumNear;
    }

    public float getFar() {
        return mFrustumFar;
    }

    protected void setProjection(float left, float right, float bottom, float top, float near, float far) {
        mFrustumLeft = left;
        mFrustumRight = right;
        mFrustumBottom = bottom;
        mFrustumTop = top;
        mFrustumNear = near;
        mFrustumFar = far;
    }

    /**
     * Determine whether the camera is currently in the process of transforming to another configuration.
     *
     * @return True if a transformation is ongoing, false otherwise.
     */
    public boolean isTransforming() {
        // Used to check whether a projection re-calculation is required at render time,
        // or when a pipeline transition should be disallowed.
        return (mTransformation != null && !mTransformation.isComplete());
    }

    public void setProjectionMatrix(float[] projectionMatrix, int offset, int width, int height) {

        // Fetch current transformation state
        if (mTransformation != null) {
            Camera newCamera = mTransformation.getTransformation();
            mScaleFactor = newCamera.mScaleFactor;
        }

        // Display a unit square with correct aspect ratio, regardless of screen orientation
        if (width >= height) {
            float ratio = (float) width / height;
            Matrix.frustumM(projectionMatrix, offset, -ratio * mScaleFactor, ratio * mScaleFactor, mFrustumBottom
                    * mScaleFactor, mFrustumTop * mScaleFactor, mFrustumNear, mFrustumFar);

        } else {
            float ratio = (float) height / width;
            Matrix.frustumM(projectionMatrix, offset, mFrustumLeft * mScaleFactor, mFrustumRight * mScaleFactor, -ratio
                    * mScaleFactor, ratio * mScaleFactor, mFrustumNear, mFrustumFar);

        }
    }

    @Override
    public String toString() {
        StringBuilder summary = new StringBuilder("View Parameters\n");
        summary.append("Eye point: " + getEye() + "\n");
        summary.append("Focus point: " + getFocus() + "\n");
        summary.append("Up direction: " + getUp() + "\n");
        summary.append("\nProjection parameters\n");
        summary.append("Left " + getLeft() + ", Right " + getRight() + ", Bottom " + getBottom() + ", Top " + getTop()
                + ", Near " + getNear() + ", Far " + getFar() + "\n");
        summary.append("\nDynamic parameters\n");
        summary.append("Rotation " + getRotation() + ", Scale factor " + getScaleFactor());
        return summary.toString();
    }

    @Override
    public Camera clone() {
        Camera cloned = new Camera(getEye(), getFocus(), getUp(), getLeft(), getRight(), getBottom(), getTop(), getNear(),
                getFar());
        cloned.setRotation(getRotation());
        cloned.setScaleFactor(getScaleFactor());
        return cloned;
    }

    @Override
    public boolean equals(Object object) {

        if (object.getClass() != Camera.class)
            return false;

        Camera that = (Camera) object;
        boolean equal = true;
        equal &= this.getEye().equals(that.getEye());
        equal &= this.getFocus().equals(that.getFocus());
        equal &= this.getUp().equals(that.getUp());
        equal &= (this.getLeft() == that.getLeft() && this.getRight() == that.getRight() &&
                this.getBottom() == that.getBottom() && this.getTop() == that.getTop() &&
                this.getNear() == that.getNear() && this.getFar() == that.getFar());
        equal &= this.getScaleFactor() == that.getScaleFactor();
        equal &= this.getRotation() == that.getRotation();

        return equal;
    }

}