package uk.co.ryft.pipeline.model.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.shapes.GL_Primitive;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.ui.setup.builders.BuildPrimitiveActivity;
import android.app.Activity;
import android.opengl.GLES20;

public class Primitive implements Element {

    private static final long serialVersionUID = -3522126203623788186L;

    public static enum Type implements ElementType {
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

        @Override
        public String getDescription() {
            return mDescriptionMap.get(this);
        }

        @Override
        public Class<? extends Activity> getEditorActivity() {
            return BuildPrimitiveActivity.class;
        }

        public int getGLPrimitive() {
            return mPrimitiveMap.get(this);
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }
    };

    protected final Type mType;
    protected final ArrayList<Float3> mVertices;
    protected final Colour mColour;

    public Primitive(Type type) {
        mType = type;
        mVertices = new ArrayList<Float3>();
        mColour = Colour.WHITE;
    }

    public Primitive(Type type, List<Float3> vertices) {
        mType = type;
        // XXX This is safe because Float3s are immutable.
        mVertices = new ArrayList<Float3>(vertices);
        mColour = Colour.WHITE;
    }

    public Primitive(Type type, List<Float3> vertices, Colour colour) {
        mType = type;
        mVertices = new ArrayList<Float3>(vertices);
        mColour = colour;
    }

    public Type getType() {
        return mType;
    }

    public List<Float3> getVertices() {
        return new ArrayList<Float3>(mVertices);
    }

    public Colour getColour() {
        return mColour;
    }

    public int getColourArgb() {
        return mColour.toArgb();
    }

    /**
     * Instantiates a new Drawable object which represents this element, and is ready to draw in the
     * renderer.
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
        for (Float3 fp : getVertices()) {
            coords[i] = fp.getX();
            coords[i + 1] = fp.getY();
            coords[i + 2] = fp.getZ();
            i += 3;
        }
        float[] colour = getColour().toArray();

        return new GL_Primitive(coords, colour, getType().getGLPrimitive());
    }

    public int getIconRef() {
        return R.drawable.ic_action_scene;
    }

    @Override
    public String getTitle() {
        return mType.getDescription() + "\n(" + mType.toString() + ")";
    }

    @Override
    public String getSummary() {
        String summary = "Consists of " + mVertices.size();
        if (mVertices.size() == 1)
            summary += " vertex.";
        else
            summary += " vertices.";
        return summary;
    }

    @Override
    public int getPrimitiveCount() {
        return 1;
    }

    @Override
    public int getVertexCount() {
        return mVertices.size();
    }

    @Override
    public String toString() {
        String details = getTitle() + "\n" + getSummary() + "\n";
        for (Float3 p : mVertices)
            details += "\n" + p.toString();
        return details;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Primitive translate(float x, float y, float z) {
        ArrayList<Float3> vertices = new ArrayList<Float3>();
        for (Float3 v : mVertices)
            vertices.add(v.translate(x, y, z));
        return new Primitive(getType(), vertices, getColour());
    }

    @Override
    public Primitive translate(Float3 v) {
        return translate(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Primitive rotate(float a, float x, float y, float z) {
        ArrayList<Float3> vertices = new ArrayList<Float3>();
        for (Float3 v : mVertices)
            vertices.add(v.rotate(a, x, y, z));

        // TODO: Decide whether this (and other set operations) should instantiate a new Primitive
        // (immutable) or perform operations on itself (mutable) and document this.

        return new Primitive(getType(), vertices, getColour());
    }

    @Override
    public Primitive rotate(float a, Float3 v) {
        return rotate(a, v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Object clone() {
        return new Primitive(getType(), getVertices(), getColour());
    }

}
