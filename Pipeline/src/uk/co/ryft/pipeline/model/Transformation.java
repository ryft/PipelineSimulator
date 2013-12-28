package uk.co.ryft.pipeline.model;

import android.os.SystemClock;

public abstract class Transformation {
    
    protected long mStartTime = SystemClock.uptimeMillis();
    protected long mLength = 2000;
    
    protected abstract float[] getTransformationState(float progress);
    
    public float[] getTransformation(long time) {
        
        if (time < mStartTime)
            return getTransformationState(0);
        
        else if (time > mStartTime + mLength)
            return getTransformationState(1);
        
        else {
            float elapsed = time - mStartTime;
            float progress = elapsed / mLength;
            return getTransformationState(progress);
        }
    }

}
