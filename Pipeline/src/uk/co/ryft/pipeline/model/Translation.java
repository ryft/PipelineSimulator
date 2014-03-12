package uk.co.ryft.pipeline.model;

import uk.co.ryft.pipeline.gl.Float3;
import android.opengl.Matrix;

// XXX: Explain the rationale behind this being immutable
public class Translation extends Transformation<float[]> {

    private final Float3 mTranslation;

    private float[] mCurrentState = new float[16];

    public Translation(Float3 translation, int duration) {
        super(duration);
        mTranslation = translation;
    }

    @Override
    protected float[] getTransformationState(float progress) {
        Matrix.setIdentityM(mCurrentState, 0);
        Float3 step = mTranslation.scale(progress);
        Matrix.translateM(mCurrentState, 0, step.getX(), step.getY(), step.getZ());
        
        return mCurrentState;
    }

}
