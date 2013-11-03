package uk.co.ryft.pipeline.model;

import android.opengl.Matrix;
import uk.co.ryft.pipeline.gl.FloatPoint;

public class Rotation implements Transformation {

    private final FloatPoint mAxis;
    private final float mAngle;
    private final int mTotalIterations;
    private int mIterationsComplete = 0;

    private float[] mCurrentState = new float[16];

    public Rotation(float angle, FloatPoint axis, int steps) {
        mTotalIterations = steps;
        mAxis = axis; // TODO: unsafe.
        mAngle = angle / steps;
        
        Matrix.setIdentityM(mCurrentState, 0);
    }

    @Override
    public boolean hasNext() {
        return (mIterationsComplete < mTotalIterations);
    }

    @Override
    public float[] next() {
        if (hasNext()) {
            Matrix.setIdentityM(mCurrentState, 0);
            Matrix.rotateM(mCurrentState, 0, mAngle * (mIterationsComplete + 1), mAxis.getX(), mAxis.getY(), mAxis.getZ());
            mIterationsComplete++;
        }
        return mCurrentState; // XXX safe because primitive. (I think)
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
                "Unsupported operation: attempting to remove a transformation step from a transition.");
    }

}
