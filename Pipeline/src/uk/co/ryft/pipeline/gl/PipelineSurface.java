package uk.co.ryft.pipeline.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import uk.co.ryft.pipeline.model.Element;

import java.util.List;

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
    
    private void construct() { //TODO is this ever called?

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
    }
    
    public void updateScene(List<Element> elements) {
        mRenderer.updateScene(elements);
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

}
