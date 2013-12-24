package uk.co.ryft.pipeline.model;

import android.opengl.Matrix;
import uk.co.ryft.pipeline.gl.Float3;

public class Rotation extends Transformation {

    private final Float3 mAxis;
    private final float mAngle;

    private float[] mCurrentState = new float[16];

    public Rotation(float angle, Float3 axis) {
        mAxis = (Float3) axis.clone();
        mAngle = angle;
    }

    @Override
    protected float[] getTransformationState(float progress) {
        Matrix.setIdentityM(mCurrentState, 0);
        Matrix.rotateM(mCurrentState, 0, mAngle * progress, mAxis.getX(), mAxis.getY(), mAxis.getZ());
        
        return mCurrentState;
    }

}
