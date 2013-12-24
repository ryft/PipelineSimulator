package uk.co.ryft.pipeline.model.shapes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.shapes.GL_Composite;
import uk.co.ryft.pipeline.model.Element;

// XXX Implementation of Drawable which uses one or more GL ES 2 primitives.
public class Composite implements Element {
    
    private static final long serialVersionUID = -6787922946411889774L;

    public static enum Type {
        CONVEX_POLYGON, CUSTOM_SHAPE;

        private static final Map<Type, String> mDescriptionMap;
        static {
            Map<Type, String> descriptionMap = new HashMap<Type, String>();
            descriptionMap.put(Type.CONVEX_POLYGON, "Convex Polygon");
            descriptionMap.put(Type.CUSTOM_SHAPE, "Custom Composite");
            mDescriptionMap = Collections.unmodifiableMap(descriptionMap);
        }
        
        public String getDescription() {
            return mDescriptionMap.get(this);
        }
    };
    
    protected Type mType;
    protected final LinkedList<Element> mComponents;
    
    public Composite(Type type, Collection<? extends Element> elements) {
        mType = type;
        mComponents = new LinkedList<Element>();
        mComponents.addAll(elements);
    }
    
    public Collection<Element> getComponents() {
        // TODO decide whether or not this should be made safe
        return mComponents;
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
    public int getIconRef() {
        // TODO
        return R.drawable.ic_action_element;
    }

    @Override
    public CharSequence getTitle() {
        // TODO Auto-generated method stub
        return mType.getDescription();
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
    public String toString() {
        String details = getTitle() + "\n" + getSummary() + "\n";
        for (Element e : mComponents)
            details += "\n" + e.toString();
        return details;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public Composite translate(float x, float y, float z) {
        for (Element e : mComponents)
            e.translate(x, y, z);
        return this;
    }

    @Override
    public Composite translate(Float3 v) {
        for (Element e : mComponents)
            e.translate(v);
        return this;
    }

    @Override
    public Composite rotate(float a, float x, float y, float z) {
        for (Element e : mComponents)
            e.rotate(a, x, y, z);
        return this;
    }

    @Override
    public Composite rotate(float a, Float3 v) {
        for (Element e : mComponents)
            e.rotate(a, v);
        return this;
    }

    @Override
    public Object clone() {
        
        LinkedList<Element> components = (LinkedList<Element>) mComponents.clone();
        return new Composite(mType, components);
    }

}
