package uk.co.ryft.pipeline.gl;

import android.opengl.Matrix;

public class Float3Wrapper {

    private Float3 mWrapped;

    public Float3Wrapper(Float3 toWrap) {
        mWrapped = toWrap;
    }
    
    public void wrap(Float3 toWrap) {
        mWrapped = toWrap;
    }

    public Float3 unwrap() {
        return mWrapped;
    }

    public Float3Wrapper transform(float[] transformation) {

        float[] v = new float[] { mWrapped.getX(), mWrapped.getY(), mWrapped.getZ(), 1 };
        Matrix.multiplyMV(v, 0, transformation, 0, v, 0);
        return new Float3Wrapper(new Float3(v[0], v[1], v[2]));
    }
    
    public String toString() {
        return mWrapped.toString();
    }

}
