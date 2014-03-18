package uk.co.ryft.pipeline.model.element;

import java.io.Serializable;

import android.app.Activity;
import uk.co.ryft.pipeline.model.Drawable;
import uk.co.ryft.pipeline.model.Float3;

public interface Element extends Serializable, Cloneable {

    public ElementType getType();

    public Drawable getDrawable();

    public int getIconRef();

    public CharSequence getTitle();

    public CharSequence getSummary();

    public int getPrimitiveCount();

    public int getVertexCount();

    @Override
    public String toString();

    public boolean isPrimitive();

    public Element translate(float x, float y, float z);

    public Element translate(Float3 v);

    // XXX Rotates about the origin.
    public Element rotate(float a, float x, float y, float z);

    public Element rotate(float a, Float3 v);

    public Object clone();

    public interface ElementType {

        public boolean isPrimitive();

        public String getDescription();

        public Class<? extends Activity> getEditorActivity();

    }

}
