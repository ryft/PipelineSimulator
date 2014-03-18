package uk.co.ryft.pipeline.model;

import uk.co.ryft.pipeline.model.lighting.LightingModel;

public interface Drawable {
    
    public abstract void draw(LightingModel lightingModel, float[] mvMatrix, float[] mvpMatrix);

}
