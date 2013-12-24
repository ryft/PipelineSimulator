package uk.co.ryft.pipeline.ui;

import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class PipelineSurface extends GLSurfaceView {

    final PipelineRenderer mRenderer = new PipelineRenderer();

    public PipelineSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct(context);
    }

    public PipelineSurface(Context context) {
        super(context);
        construct(context);
    }

    private void construct(Context context) {
        
        final GestureDetector gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
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
        
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!gestureDetector.onTouchEvent(event))
                    onSceneMove(event);
                
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
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    private boolean mEditMode;
    
    public boolean isEditMode() {
        return mEditMode;
    }
    
    public void toggleEditMode() {
        mEditMode = !mEditMode;

        if (mEditMode)
            setBackgroundResource(R.drawable.surface_border);
        else
            setBackgroundResource(0);
    }

    public void toggle() {
        mRenderer.interact();
        requestRender();
    }

    public void updateScene(List<Element> elements) {
        mRenderer.updateScene(elements);
        requestRender();
    }

    private float mPreviousX = 0;
    private float mPreviousY = 0;
    private float TOUCH_SCALE_FACTOR = 0.3f;

    public void onSceneMove(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        
        if (!mEditMode)
            return;

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

                mRenderer.mAngle -= (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
    }

}
