package uk.co.ryft.pipeline.test.model;

import android.graphics.Color;

import junit.framework.TestCase;

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
