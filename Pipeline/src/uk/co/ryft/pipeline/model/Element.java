package uk.co.ryft.pipeline.model;

import java.io.Serializable;

import uk.co.ryft.pipeline.gl.Drawable;

public interface Element extends Comparable<Element>, Serializable, Cloneable {
    
    public Drawable getDrawable();

    public int getIconRef();

    public CharSequence getTitle();

    public CharSequence getSummary();
    
    public boolean isPrimitive();

}
