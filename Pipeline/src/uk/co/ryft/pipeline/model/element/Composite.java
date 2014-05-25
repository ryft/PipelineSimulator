package uk.co.ryft.pipeline.model.element;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.element.drawable.Drawable;
import uk.co.ryft.pipeline.model.element.drawable.GL_Composite;
import uk.co.ryft.pipeline.ui.setup.builder.BuildCameraActivity;
import uk.co.ryft.pipeline.ui.setup.builder.BuildCuboidActivity;
import uk.co.ryft.pipeline.ui.setup.builder.BuildCylinderActivity;
import android.app.Activity;

// XXX Implementation of Drawable which uses one or more GL ES 2 primitives.
public class Composite implements Element {
    
    private static final long serialVersionUID = -6787922946411889774L;

    public static enum Type implements ElementType {
        CYLINDER, CUBOID, CAMERA, CUSTOM;

        private static final Map<Type, String> mDescriptionMap;
        static {
            Map<Type, String> descriptionMap = new HashMap<Type, String>();
            descriptionMap.put(Type.CYLINDER, "Cylinder");
            descriptionMap.put(Type.CUBOID, "Cuboid");
            descriptionMap.put(Type.CAMERA, "Camera");
            descriptionMap.put(Type.CUSTOM, "Composite Shape");
            mDescriptionMap = Collections.unmodifiableMap(descriptionMap);
        }

        private static final Map<Type, Class<? extends Activity>> mEditorMap;
        static {
            Map<Type, Class<? extends Activity>> editorMap = new HashMap<Type, Class<? extends Activity>>();
            editorMap.put(Type.CYLINDER, BuildCylinderActivity.class);
            editorMap.put(Type.CUBOID, BuildCuboidActivity.class);
            editorMap.put(Type.CAMERA, BuildCameraActivity.class);
            editorMap.put(Type.CUSTOM, null); // Should never be called
            mEditorMap = Collections.unmodifiableMap(editorMap);
        }

        @Override
        public String getDescription() {
            return mDescriptionMap.get(this);
        }

        @Override
        public Class<? extends Activity> getEditorActivity() {
            return mEditorMap.get(this);
        }

        @Override
        public final boolean isPrimitive() {
            return false;
        }
    };
    
    protected final Type mType;
    protected final LinkedList<Element> mComponents;
    
    public Composite(Type type, Collection<? extends Element> elements) {
        mType = type;
        mComponents = new LinkedList<Element>();
        mComponents.addAll(elements);
    }

    public Type getType() {
        return mType;
    }
    
    public Collection<Element> getComponents() {
        // XXX The immutability of elements makes this safe.
        return new LinkedList<Element>(mComponents);
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
        return R.drawable.ic_action_element;
    }

    @Override
    public CharSequence getTitle() {
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
    public int getPrimitiveCount() {
        int size = 0;
        for (Element e : mComponents)
            size += e.getPrimitiveCount();
        return size;
    }

    @Override
    public int getVertexCount() {
        int size = 0;
        for (Element e : mComponents)
            size += e.getVertexCount();
        return size;
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
        LinkedList<Element> components = new LinkedList<Element>();
        for (Element e : mComponents)
            components.add(e.translate(x, y, z));
        return new Composite(getType(), components);
    }

    @Override
    public Composite translate(Float3 v) {
        return translate(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Composite rotate(float a, float x, float y, float z) {
        LinkedList<Element> components = new LinkedList<Element>();
        for (Element e : mComponents)
            components.add(e.rotate(a, x, y, z));
        return new Composite(getType(), components);
    }

    @Override
    public Composite rotate(float a, Float3 v) {
        return rotate(a, v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Object clone() {
        return new Composite(getType(), getComponents());
    }

}
