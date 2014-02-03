package uk.co.ryft.pipeline.model;

import android.os.SystemClock;

public abstract class Transformation<E> {
    
    protected long mStartTime = SystemClock.uptimeMillis();
    protected long mLength = 2000;
    
    protected abstract E getTransformationState(float progress);
    
    public E getTransformation(long time) {
        
        if (isComplete(time))
            return getTransformationState(1);
        
        else if (time < mStartTime)
            return getTransformationState(0);
        
        else {
            float elapsed = time - mStartTime;
            float progress = elapsed / mLength;
            return getTransformationState(progress);
        }
    }
    
    public boolean isComplete(long time) {
        if (time > mStartTime + mLength)
            return true;
        else
            return false;
    }

}
