package uk.co.ryft.pipeline.model;

import java.io.Serializable;

import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.shapes.ElementType;

public interface Element extends Serializable, Cloneable {

    public ElementType getType();
    
    public Drawable getDrawable();

    public int getIconRef();

    public CharSequence getTitle();

    public CharSequence getSummary();

    public int getSize();
    
    @Override
    public String toString();

    public boolean isPrimitive();

    public Element translate(float x, float y, float z);
    public Element translate(Float3 v);
    
    // XXX Rotates about the origin.
    public Element rotate(float a, float x, float y, float z);
    public Element rotate(float a, Float3 v);
    
    public Object clone();

}
