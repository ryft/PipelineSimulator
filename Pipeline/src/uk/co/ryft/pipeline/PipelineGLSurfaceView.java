package uk.co.ryft.pipeline;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class PipelineGLSurfaceView extends GLSurfaceView {

    private final PipelineRenderer mRenderer;

    public PipelineGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new PipelineRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
        
    }

}
