package uk.co.ryft.pipeline.gl;

public class Colour {
    
    float red;
    float green;
    float blue;
    float alpha;

    public Colour(int red, int green, int blue) {
        this.red = (float) red / 255;
        this.green = (float) green / 255;
        this.blue = (float) blue / 255;
        this.alpha = (float) 1f;
    }
    
    public Colour(int red, int green, int blue, int alpha) {
        this.red = (float) red / 255;
        this.green = (float) green / 255;
        this.blue = (float) blue / 255;
        this.alpha = (float) alpha / 255;
    }

    public float[] getColour() {
        float[] c = { red, green, blue, alpha };
        return c;
    }

}
