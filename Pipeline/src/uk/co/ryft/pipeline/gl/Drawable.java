package uk.co.ryft.pipeline.gl;

import uk.co.ryft.pipeline.gl.lighting.LightingModel;


public interface Drawable {
    
    public abstract void draw(LightingModel lightingModel, float[] mvMatrix, float[] mvpMatrix);

}
