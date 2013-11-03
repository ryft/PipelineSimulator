
package uk.co.ryft.pipeline.model.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.FloatPoint;
import uk.co.ryft.pipeline.gl.shapes.GL_Primitive;
import uk.co.ryft.pipeline.model.Element;
import android.opengl.GLES20;

public class Primitive implements Element {

    private static final long serialVersionUID = -3522126203623788186L;

    public static enum Type {
        GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN;

        private static final Map<Type, String> mDescriptionMap;
        static {
            Map<Type, String> descriptionMap = new HashMap<Type, String>();
            descriptionMap.put(Type.GL_POINTS, "Single points");
            descriptionMap.put(Type.GL_LINES, "Single lines");
            descriptionMap.put(Type.GL_LINE_STRIP, "Line strip");
            descriptionMap.put(Type.GL_LINE_LOOP, "Line loop");
            descriptionMap.put(Type.GL_TRIANGLES, "Single triangles");
            descriptionMap.put(Type.GL_TRIANGLE_STRIP, "Triangle strip");
            descriptionMap.put(Type.GL_TRIANGLE_FAN, "Triangle fan");
            mDescriptionMap = Collections.unmodifiableMap(descriptionMap);
        }

        private static final Map<Type, Integer> mPrimitiveMap;
        static {
            Map<Type, Integer> primitiveMap = new HashMap<Type, Integer>();
            primitiveMap.put(Type.GL_POINTS, GLES20.GL_POINTS);
            primitiveMap.put(Type.GL_LINES, GLES20.GL_LINES);
            primitiveMap.put(Type.GL_LINE_STRIP, GLES20.GL_LINE_STRIP);
            primitiveMap.put(Type.GL_LINE_LOOP, GLES20.GL_LINE_LOOP);
            primitiveMap.put(Type.GL_TRIANGLES, GLES20.GL_TRIANGLES);
            primitiveMap.put(Type.GL_TRIANGLE_STRIP, GLES20.GL_TRIANGLE_STRIP);
            primitiveMap.put(Type.GL_TRIANGLE_FAN, GLES20.GL_TRIANGLE_FAN);
            mPrimitiveMap = Collections.unmodifiableMap(primitiveMap);
        }
        
        public String getDescription() {
            return mDescriptionMap.get(this);
        }
        
        public Integer getGLPrimitive() {
            return mPrimitiveMap.get(this);
        }
    };

    protected Type mType;
    protected final List<FloatPoint> mVertices = new ArrayList<FloatPoint>();
    protected Colour mColour = Colour.WHITE;

    public Primitive(Type type) {
    }

    public Primitive(Type type, List<FloatPoint> vertices) {
        mType = type;
        mVertices.addAll(vertices);
    }

    public Primitive(Type type, List<FloatPoint> vertices, Colour colour) {
        mType = type;
        mVertices.addAll(vertices);
        mColour = colour;
    }

    public int getColourArgb() {
        return mColour.toArgb();
    }
    
    public void setColour(int red, int green, int blue, int alpha) {
        mColour.setColour(red, green, blue, alpha);
    }
    
    public void setColour(int red, int green, int blue) {
        mColour.setColour(red, green, blue);
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type t) {
        mType = t;
    }

    public List<FloatPoint> getVertices() {
        // TODO this is unsafe -- passing a reference to a list.
        return mVertices;
    }
    
    public void setVertices(List<FloatPoint> vertices) {
        // Probably not unsafe but investigate and discuss the mutability of FloatPoints.
        mVertices.clear();
        mVertices.addAll(vertices);
    }

    public String getTitle() {
        return mType.getDescription() + "\n(" + mType.toString() + ")";
    }

    public String getSummary() {
        String summary = "Consists of " + mVertices.size();
        if (mVertices.size() == 1)
            summary += " vertex.";
        else
            summary += " vertices.";
        return summary;
    }

    public int getIconRef() {
        return R.drawable.ic_action_scene; // TODO
    }

    /**
     * Instantiates a new Drawable object which represents this element, and is
     * ready to draw in the renderer.
     * 
     * This function must be called from the render thread, not the UI thread.
     * 
     * @return Drawable representation of this element.
     */
    @Override
    public Drawable getDrawable() {
        
        // Convert vertices to float array
        int vertexCount = getVertices().size() * 3;
        float[] coords = new float[vertexCount];
        int i = 0;
        for (FloatPoint fp : getVertices()) {
            coords[i] = fp.getX();
            coords[i + 1] = fp.getY();
            coords[i + 2] = fp.getZ();
            i += 3;
        }
        float[] colour = getColour().toArray();

        return new GL_Primitive(coords, colour, vertexCount, getType().getGLPrimitive());
    }

    public Colour getColour() {
        return mColour;
    }

    public void setColour(Colour colour) {
        mColour = colour;
    }

    @Override
    public Object clone() {
        return new Primitive(mType, mVertices, mColour);
    }

    @Override
    public int compareTo(Element arg0) {
        // Compare z-orders. TODO: cache these min/max values for faster
        // sorting.
        return 0;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

}
