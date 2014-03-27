package uk.co.ryft.pipeline.ui.pipeline;

import uk.co.ryft.pipeline.R;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class PipelineSurface extends GLSurfaceView {

    @SuppressWarnings("unused")
    private static final String TAG = "PipelineSurface";

    private final PipelineRenderer mRenderer;

    public PipelineRenderer getRenderer() {
        // This is not really unsafe as mRenderer is a final member
        return mRenderer;
    }

    protected Context mContext;

    public PipelineSurface(Context context) {
        super(context);
        throw new RuntimeException("Pipeline surface called with no parameters");
    }

    public PipelineSurface(Context context, Bundle params, boolean multisample) {
        super(context);
        mContext = context;
        mRenderer = new PipelineRenderer(params, multisample);
        
        int minSamples = params.getInt("min_samples", 2);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        if (multisample)
            setEGLConfigChooser(new MultisampleConfigChooser(minSamples));
        else
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(mRenderer);

        // Render the view continuously so we can support transition effects.
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @Override
    public void setAlpha(float alpha) {
        mRenderer.setGlobalLightLevel(alpha);
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

                mRenderer.updateRotation((dx + dy) * TOUCH_SCALE_FACTOR); // = 180.0f / 320
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }

}
