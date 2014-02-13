package uk.co.ryft.pipeline.gl;

import java.io.Serializable;
import java.util.Random;

import android.graphics.Color;

public class Colour implements Serializable, Cloneable {
    
    private static final long serialVersionUID = -6574165998266076014L;
    
    protected int red;
    protected int green;
    protected int blue;
    protected int alpha;

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
    
    // Randomised colour, for testing purposes
    public static final Colour RANDOM = new RandomColour();

    public Colour(int red, int green, int blue) {
        setColour(red, green, blue);
    }
    
    public Colour(int red, int green, int blue, int alpha) {
        setColour(red, green, blue, alpha);
    }
    
    public void setColour(int red, int green, int blue) {
        setColour(red, green, blue, 255);
    }
    
    private void setColour(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float[] toArray() {
        float[] c = { red / 255f, green / 255f, blue / 255f, alpha / 255f };
        return c;
    }
    
    public int toArgb() {
        return Color.argb(alpha, red, green, blue);
    }
    
    public static Colour fromArgb(int argb) {
        return new Colour(Color.red(argb), Color.green(argb), Color.blue(argb), Color.alpha(argb));
    }
    
    @Override
    public String toString() {
        return "("+red+", "+green+", "+blue+"): "+alpha;
        
//        return "#"
//              + toHex((int) Math.floor(red / 16))
//              + toHex((int) Math.floor(red % 16))
//              + toHex((int) Math.floor(green / 16))
//              + toHex((int) Math.floor(green % 16))
//              + toHex((int) Math.floor(blue / 16))
//              + toHex((int) Math.floor(blue % 16));
    }
    
//    private String toHex(int decimal) {
//        // XXX Convert one decimal number in the range [0..16) to hex
//        String letters[] = {"A", "B", "C", "D", "E", "F"};
//        if (decimal >= 0 && decimal < 10)
//            return String.valueOf(decimal);
//        else if (decimal < 16)
//            return letters[decimal-10];
//        else
//            throw new RuntimeException("toHex() only accepts integers in the range [0 .. 16), "+decimal+" was provided.");
//    }

    @Override
    public Object clone() {
        return new Colour(red, green, blue, alpha);
    }
    
    // Randomises its colour on each call of toArray()
    static class RandomColour extends Colour {

        private static final long serialVersionUID = -5270392509485931336L;
        private static Random r = new Random();

        public RandomColour() {
            super(r.nextInt(256), r.nextInt(256), r.nextInt(256));
        }
        
        @Override
        public float[] toArray() {
            randomise();
            return super.toArray();
        }
        
        public int toArgb() {
            randomise();
            return super.toArgb();
        }
        
        private void randomise() {
            red = r.nextInt(256);
            green = r.nextInt(256);
            blue = r.nextInt(256);
        }
        
    }

}
