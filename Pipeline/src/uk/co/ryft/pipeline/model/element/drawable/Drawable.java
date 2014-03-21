package uk.co.ryft.pipeline.model.element.drawable;

import uk.co.ryft.pipeline.model.lighting.LightingModel;

public interface Drawable {
    
    public void draw(LightingModel lightingModel, float[] mvMatrix, float[] mvpMatrix);

}
