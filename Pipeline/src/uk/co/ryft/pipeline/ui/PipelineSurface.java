package uk.co.ryft.pipeline.ui;

import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PipelineSurface extends GLSurfaceView {

    private final PipelineRenderer mRenderer = new PipelineRenderer();

    public PipelineSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public PipelineSurface(Context context) {
        super(context);
        construct();
    }

    private void construct() { // TODO is this ever called?

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(mRenderer);

        // Render the view continuously so we can support transition effects.
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

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

    private boolean mEditMode;

    public void setEditMode(boolean editMode) {
        mEditMode = editMode;
        if (editMode)
            setBackgroundResource(R.drawable.surface_border);
        else
            setBackgroundResource(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        
        if (!mEditMode)
            return false;

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
        return true;
    }

}
