package uk.co.ryft.pipeline.gl;

import java.io.Serializable;

public class Colour implements Serializable {
    
    private static final long serialVersionUID = -6574165998266076014L;
    
    float red;
    float green;
    float blue;
    float alpha;

    // Primaries
    public static final Colour RED = new Colour(255, 0, 0);
    public static final Colour GREEN = new Colour(0, 255, 0);
    public static final Colour BLUE = new Colour(0, 0, 255);

    // Secondaries
    public static final Colour YELLOW = new Colour(255, 255, 0);
    public static final Colour MAGENTA = new Colour(255, 0, 255);
    public static final Colour CYAN = new Colour(0, 255, 255);

    // Shades
    public static final Colour WHITE = new Colour(255, 255, 255);
    public static final Colour GREY = new Colour(127, 127, 127);
    public static final Colour BLACK = new Colour(0, 0, 0);

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

    public float[] toArray() {
        float[] c = { red, green, blue, alpha };
        return c;
    }

}
