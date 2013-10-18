package uk.co.ryft.pipeline.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.shapes.GL_Composite;

// XXX Implementation of Drawable which uses one or more GL ES 2 primitives.
public class Composite implements Element {
    
    private static final long serialVersionUID = -6787922946411889774L;
    
    protected final List<Element> mComponents;
    
    public Composite(Collection<? extends Element> elements) {
        
        mComponents = new LinkedList<Element>();
        mComponents.addAll(elements);
    }

    @Override
    public Drawable getDrawable() {
        
        // Recursively get drawable components
        LinkedList<Drawable> drawables = new LinkedList<Drawable>();
        for (Element e : mComponents)
            drawables.add(e.getDrawable());
        
        return new GL_Composite(drawables);
    }

    @Override
    public int compareTo(Element another) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIconRef() {
        // TODO
        return R.drawable.ic_action_scene;
    }

    @Override
    public CharSequence getTitle() {
        // TODO Auto-generated method stub
        return "Composite";
    }

    @Override
    public CharSequence getSummary() {
        String summary = "Consists of " + mComponents.size();
        if (mComponents.size() == 1)
            summary += " component.";
        else
            summary += " components.";
        return summary;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    


}
