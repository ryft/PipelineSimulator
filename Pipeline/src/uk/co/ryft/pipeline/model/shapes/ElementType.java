package uk.co.ryft.pipeline.model.shapes;

import android.app.Activity;

public interface ElementType {

    public boolean isPrimitive();
    public String getDescription();
    public Class<? extends Activity> getEditorActivity();
    
}
