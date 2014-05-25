package uk.co.ryft.pipeline.model.element;

import android.app.Activity;

import java.io.Serializable;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.element.drawable.Drawable;

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

    // Type interface to be implemented by each primitive and composite type
    public interface ElementType {

        /**
         * @return True if this type refers to an OpenGL primitive, false otherwise.
         */
        public boolean isPrimitive();

        /**
         * @return A brief (couple of words max) description of this element type.
         */
        public String getDescription();

        /**
         * Get a reference to the class used to add or edit this element type.
         * Must accept an intent containing the boolean value 'edit_mode'.
         * If 'edit_mode' == true, must also use the provided element 'element'
         * as a basis.
         *
         * On finishing with result code RESULT_OK, activity must provide an
         * intent containing new element 'element'.
         *
         * @return Activity class reference if this type can be edited, null otherwise.
         */
        public Class<? extends Activity> getEditorActivity();

    }

}
