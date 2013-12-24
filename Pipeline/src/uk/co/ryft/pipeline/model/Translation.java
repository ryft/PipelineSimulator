package uk.co.ryft.pipeline.model;

import uk.co.ryft.pipeline.gl.Float3;
import android.opengl.Matrix;

// XXX: Explain the rationale behind this being immutable
public class Translation implements Transformation {

    private final Float3 mTranslation;
    private final int mTotalIterations;
    private int mIterationsComplete = 0;

    private float[] mCurrentState = new float[16];

    public Translation(Float3 translation, int steps) {
        mTotalIterations = steps;
        mTranslation = new Float3(translation.getX() / steps, translation.getY() / steps,
                translation.getZ() / steps);
        
        Matrix.setIdentityM(mCurrentState, 0);
    }

    @Override
    public boolean hasNext() {
        return (mIterationsComplete < mTotalIterations);
    }

    @Override
    public float[] next() {
        // XXX: this is an important mechanic for the moment. Even if we're done, we return the
        // completed transformation matrix so the renderer can just store a list of transformations,
        // completed or not.
        if (hasNext()) {
            Matrix.translateM(mCurrentState, 0, mTranslation.getX(), mTranslation.getY(),
                    mTranslation.getZ());
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
