package uk.co.ryft.pipeline.ui;

import java.util.List;

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

    public void boop() {
        mRenderer.rot += 0.1f;
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }

}
