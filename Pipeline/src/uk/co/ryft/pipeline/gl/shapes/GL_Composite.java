package uk.co.ryft.pipeline.gl.shapes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.gl.Drawable;

public class GL_Composite implements Drawable {

    protected final List<Drawable> mComponents;

    public GL_Composite(Collection<? extends Drawable> drawables) {
        
        mComponents = new LinkedList<Drawable>();
        mComponents.addAll(drawables);
    }

    public void draw(int glProgram, float[] mvMatrix, float[] mvpMatrix) {
        for (Drawable d : mComponents)
            d.draw(glProgram, mvMatrix, mvpMatrix);
    }
}