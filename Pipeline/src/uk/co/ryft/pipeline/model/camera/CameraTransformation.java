package uk.co.ryft.pipeline.model.camera;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.Transformation;

// XXX: Explain the rationale behind this being immutable
public class CameraTransformation extends Transformation<Camera> {

    @SuppressWarnings("unused")
    private static final String TAG = "CameraTransformation";

    private final Camera mOrigin;
    private final Camera mDestination;

    private final float mRotationBase;
    private final float mRotationDiff;
    private final float mScaleFactorBase;
    private final float mScaleFactorDiff;

    private final Float3 mEyeBase;
    private final Float3 mEyeDiff;
    private final Float3 mFocusBase;
    private final Float3 mFocusDiff;
    private final Float3 mUpBase;
    private final Float3 mUpDiff;

    private final float mLeftBase;
    private final float mLeftDiff;
    private final float mRightBase;
    private final float mRightDiff;
    private final float mBottomBase;
    private final float mBottomDiff;
    private final float mTopBase;
    private final float mTopDiff;
    private final float mNearBase;
    private final float mNearDiff;
    private final float mFarBase;
    private final float mFarDiff;

    public CameraTransformation(Camera origin, Camera destination, int duration) {
        super(duration);
        
        mOrigin = origin.clone();
        mDestination = destination.clone();
        
        mRotationBase = mOrigin.getRotation();
        mRotationDiff = mDestination.getRotation() - mRotationBase;
        mScaleFactorBase = mOrigin.getScaleFactor();
        mScaleFactorDiff = mDestination.getScaleFactor() - mScaleFactorBase;
        
        mEyeBase = mOrigin.getEye();
        mEyeDiff = mDestination.getEye().minus(mEyeBase);
        mFocusBase = mOrigin.getFocus();
        mFocusDiff = mDestination.getFocus().minus(mFocusBase);
        mUpBase = mOrigin.getUp();
        mUpDiff = mDestination.getUp().minus(mUpBase);

        mLeftBase = mOrigin.getLeft();
        mLeftDiff = mDestination.getLeft() - mLeftBase;
        mRightBase = mOrigin.getRight();
        mRightDiff = mDestination.getRight() - mRightBase;
        mBottomBase = mOrigin.getBottom();
        mBottomDiff = mDestination.getBottom() - mBottomBase;
        mTopBase = mOrigin.getTop();
        mTopDiff = mDestination.getTop() - mTopBase;
        mNearBase = mOrigin.getNear();
        mNearDiff = mDestination.getNear() - mNearBase;
        mFarBase = mOrigin.getFar();
        mFarDiff = mDestination.getFar() - mFarBase;
    }

    @Override
    protected Camera getTransformationState(float progress) {

        // We're required to clone here, or else the caller may alter our referenced objects
        if (progress <= 0)
            return mOrigin.clone();
        else if (progress >= 1)
            return mDestination.clone();

        Float3 eye = mEyeBase.plus(mEyeDiff.scale(progress));
        Float3 focus = mFocusBase.plus(mFocusDiff.scale(progress));
        Float3 up = mUpBase.plus(mUpDiff.scale(progress));

        float left = mLeftBase + (mLeftDiff * progress);
        float right = mRightBase + (mRightDiff * progress);
        float bottom = mBottomBase + (mBottomDiff * progress);
        float top = mTopBase + (mTopDiff * progress);
        float near = mNearBase + (mNearDiff * progress);
        float far = mFarBase + (mFarDiff * progress);
        
        Camera diff = new Camera(eye, focus, up, left, right, bottom, top, near, far);
        diff.setRotation(mRotationBase + mRotationDiff * progress);
        diff.setScaleFactor(mScaleFactorBase + mScaleFactorDiff * progress);
        return diff;
    }

}
