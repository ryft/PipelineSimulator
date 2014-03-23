package uk.co.ryft.pipeline.model.transformation;

import android.os.SystemClock;

public abstract class Transformation<E> {

    protected final long mStartTime;
    protected final long mDuration;

    /**
     * An abstract implementation of a timed transformation, on an object of generic type. The transformation begins at the time
     * of construction.
     * 
     * @param duration
     *            The duration of the transformation, in milliseconds.
     */
    public Transformation(int duration) {
        mStartTime = SystemClock.uptimeMillis();
        mDuration = duration;
    }

    protected abstract E getTransformationState(float progress);

    /**
     * Compute the current state of the transformation at the current time.
     * 
     * @return The current state of the transformation.
     */
    public E getTransformation() {
        return getTransformation(SystemClock.uptimeMillis());
    }

    /**
     * Compute the current state of the transformation at the given time.
     * 
     * @param time
     *            The system uptime, in milliseconds.
     * @return The current state of the transformation at the given time.
     */
    public E getTransformation(long time) {

        if (isComplete(time))
            return getTransformationState(1);

        else if (time < mStartTime)
            return getTransformationState(0);

        else {
            float elapsed = time - mStartTime;
            float progress = elapsed / mDuration;
            return getTransformationState(progress);
        }
    }

    /**
     * Tests whether the transformation is complete at the current time.
     * 
     * @return True if the transformation is complete; false otherwise.
     */
    public boolean isComplete() {
        return isComplete(SystemClock.uptimeMillis());
    }

    /**
     * Tests whether the transformation is complete at the given time.
     * 
     * @param time
     *            The system uptime, in milliseconds.
     * @return True if the transformation is complete; false otherwise.
     */
    public boolean isComplete(long time) {
        if (time > mStartTime + mDuration)
            return true;
        else
            return false;
    }

}
