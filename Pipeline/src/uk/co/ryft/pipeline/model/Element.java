package uk.co.ryft.pipeline.model;

import java.io.Serializable;

import uk.co.ryft.pipeline.gl.Drawable;

public interface Element extends Serializable, Cloneable {
    
    public Drawable getDrawable();

    public int getIconRef();

    public CharSequence getTitle();

    public CharSequence getSummary();
    
    @Override
    public String toString();

    public boolean isPrimitive();
    
    public Element translate(float x, float y, float z);
    
    // XXX Rotates about the origin.
    public Element rotate(float a, float x, float y, float z);
    
    public Object clone();

}
