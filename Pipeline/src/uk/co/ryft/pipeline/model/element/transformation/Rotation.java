package uk.co.ryft.pipeline.model.element.transformation;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.Transformation;
import android.opengl.Matrix;

public class Rotation extends Transformation<float[]> {

    private final Float3 mAxis;
    private final int mAngle;

    private float[] mCurrentState = new float[16];

    // XXX Angle in degrees
    public Rotation(int angle, Float3 axis, int duration) {
        super(duration);
        mAxis = axis;
        mAngle = angle;
    }

    @Override
    protected float[] getTransformationState(float progress) {
        Matrix.setIdentityM(mCurrentState, 0);
        Matrix.rotateM(mCurrentState, 0, mAngle * progress, mAxis.getX(), mAxis.getY(), mAxis.getZ());
        
        return mCurrentState;
    }

}
