package uk.co.ryft.pipeline.gl;

public interface Drawable {
    
    public abstract void draw(int glProgram, float[] mvMatrix, float[] mvpMatrix);

}
