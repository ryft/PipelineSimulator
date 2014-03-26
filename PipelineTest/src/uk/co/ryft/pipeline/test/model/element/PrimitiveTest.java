package uk.co.ryft.pipeline.test.model.element;

import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import uk.co.ryft.pipeline.model.Colour;
import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.element.Primitive;

public class PrimitiveTest extends TestCase {

    Primitive testPrimitive;

    public void setUp() throws Exception {
        super.setUp();

        Random r = new Random();
        List<Float3> vertices = new LinkedList<Float3>();
        for (int i = 0; i < r.nextInt(100) + 100; i++)
            vertices.add(new Float3(r.nextFloat() * 5, r.nextFloat() * 5, r.nextFloat() * 5));

        testPrimitive = new Primitive(Primitive.Type.GL_TRIANGLE_FAN, vertices, Colour.GREEN());
    }

    public void testGetType() throws Exception {
        assertEquals(Primitive.Type.GL_TRIANGLE_FAN, testPrimitive.getType());
    }

    public void testGetVertices() throws Exception {
        // Test the vertex list is passed safely
        List<Float3> vertices = testPrimitive.getVertices();
        int size = vertices.size();
        vertices.clear();
        assertEquals(size, testPrimitive.getVertexCount());
    }

    public void testGetColour() throws Exception {
        testPrimitive.getColour().setColour(255, 0, 0);
        assertEquals(Colour.RED(), testPrimitive.getColour());
    }

    public void testGetPrimitiveCount() throws Exception {
        assertEquals(1, testPrimitive.getPrimitiveCount());
    }

    public void testIsPrimitive() throws Exception {
        assertTrue(testPrimitive.isPrimitive());
    }

    public void testClone() throws Exception {
        assertEquals(testPrimitive, testPrimitive.clone());
    }
}
