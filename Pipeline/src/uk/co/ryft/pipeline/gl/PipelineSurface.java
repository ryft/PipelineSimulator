package uk.co.ryft.pipeline.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PipelineSurface extends GLSurfaceView {

    private final PipelineRenderer mRenderer = new PipelineRenderer();
    
    public PipelineSurface (Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public PipelineSurface(Context context) {
        super(context);
        construct();
    }
    
    private void construct() {

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
    
    public int zoomOut() {
        mRenderer.zoomLevel++;
        requestRender();
        return mRenderer.zoomLevel;
    }

}
