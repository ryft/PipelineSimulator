package uk.co.ryft.pipeline.test.model;

import android.graphics.Color;

import junit.framework.TestCase;

import java.util.Random;

import uk.co.ryft.pipeline.model.Colour;

public class ColourTest extends TestCase {

    Colour testColour;

    public void setUp() throws Exception {
        super.setUp();
        testColour = Colour.MAGENTA();
    }

    public void testSetColour() throws Exception {
        testColour.setColour(255, 255, 0);
        assertEquals(Colour.YELLOW(), testColour);

        // Test safety of constants
        Colour blue = Colour.BLUE();
        blue.setColour(255, 0, 0);
        assertFalse(blue.equals(Colour.BLUE()));

        // Reset colour
        testColour.setColour(255, 0, 255);
    }

    public void testToArray() throws Exception {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        int alpha = random.nextInt(256);

        Colour randomColour = new Colour(red, green, blue, alpha);
        float[] array = randomColour.toArray();
        float delta = 0.00001f;
        assertEquals(red, array[0] * 255, delta);
        assertEquals(green, array[1] * 255, delta);
        assertEquals(blue, array[2] * 255, delta);
        assertEquals(alpha, array[3] * 255, delta);
    }

    public void testToArgb() throws Exception {
        assertEquals(testColour.toArgb(), Color.MAGENTA);
    }

    public void testFromArgb() throws Exception {
        assertEquals(Colour.MAGENTA(), Colour.fromArgb(Color.MAGENTA));
    }

    public void testClone() throws Exception {
        assertEquals(testColour, testColour.clone());
    }
}
