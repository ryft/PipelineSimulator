package uk.co.ryft.pipeline.ui.simulator;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

public class PipelineSurface extends GLSurfaceView {

    private final PipelineRenderer mRenderer;
    // XXX This is very unsafe but required for saving and restoring state.
    // Can we do better by implementing it in onPause() etc here?
    public PipelineRenderer getRenderer() { return mRenderer; }
    
    protected Context mContext;
    
    public PipelineSurface(Context context) {
        super(context);
        throw new RuntimeException("Pipeline surface called with no parameters");
    }
        
    public PipelineSurface(Context context, Bundle params) {
        super(context);
        mContext = context;
        mRenderer = new PipelineRenderer(params);
        
        final GestureDetector doubleTapDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleEditMode();
                return true;
            }
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Consume all events between a double-tap to prevent "jumping"
                return true;
            }
        });
        
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, new SimpleOnScaleGestureListener() {
            
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mRenderer.setScaleFactor(detector.getScaleFactor());
                return true;
            }
        });
        
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Consume all double-tap events as highest priority
                if (!doubleTapDetector.onTouchEvent(event) && mEditMode) {

                    // XXX There is a bug in ScaleGeestureDetector where it always returns true
                    // See https://code.google.com/p/android/issues/detail?id=42591
                    scaleDetector.onTouchEvent(event);
                    onSceneMove(event);
                }
                
                return true;
            }
            
        });

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(mRenderer);

        // Render the view continuously so we can support transition effects.
        // FIXME Disabled for now, along with transitions
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    private boolean mEditMode;
    
    public boolean isEditMode() {
        return mEditMode;
    }
    
    public void toggleEditMode() {
        setEditMode(!isEditMode());
    }
    
    public void setEditMode(boolean editMode) {
        mEditMode = editMode;

        if (editMode)
            setBackgroundResource(R.drawable.surface_border);
        else
            setBackgroundResource(0);
    }

    static int mCurrentModel = 2;

    public void toggle() {
        mRenderer.setScaleFactor(1.5f);
    }

    private float mPreviousX = 0;
    private float mPreviousY = 0;
    private float TOUCH_SCALE_FACTOR = 0.3f;

    public boolean onSceneMove(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2)
                  dx = dx * -1;

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2)
                  dy = dy * -1;

                mRenderer.setRotation(mRenderer.getRotation() - (dx + dy) * TOUCH_SCALE_FACTOR);  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        
        return true;
    }

}
