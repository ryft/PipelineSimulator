package uk.co.ryft.pipeline.test.model;

import android.util.FloatMath;

import junit.framework.TestCase;

import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.test.Common;

public class Float3Test extends TestCase {

    Float3 testFloat3;
    Float3 otherFloat3;

    public void setUp() throws Exception {
        super.setUp();
        testFloat3 = new Float3(0, 1, 2);
        otherFloat3 = new Float3(1.5f, 1.5f, 1.5f);
    }

    public void testRotate() throws Exception {
        float root2 = FloatMath.sqrt(2);
        Common.assertEquals(new Float3(0, - root2 / 2, 3f / root2), testFloat3.rotate(45, 1, 0, 0));
    }

    public void testTranslate() throws Exception {
        Common.assertEquals(new Float3(1.5f, 2.5f, 3.5f), testFloat3.translate(otherFloat3.getX(), otherFloat3.getY(), otherFloat3.getZ()));
    }

    public void testPlus() throws Exception {
        Common.assertEquals(new Float3(1.5f, 2.5f, 3.5f), testFloat3.plus(otherFloat3));
    }

    public void testMinus() throws Exception {
        Common.assertEquals(new Float3(-1.5f, -0.5f, 0.5f), testFloat3.minus(otherFloat3));
    }

    public void testScale() throws Exception {
        Common.assertEquals(new Float3(0, 1.5f, 3), testFloat3.scale(1.5f));
    }

    public void testDot() throws Exception {
        assertEquals(4.5f, testFloat3.dot(otherFloat3));
    }

    public void testCross() throws Exception {
        Common.assertEquals(new Float3(-1.5f, 3, -1.5f), testFloat3.cross(otherFloat3));
    }

    public void testNormalised() throws Exception {
        float root5 = FloatMath.sqrt(5);
        Common.assertEquals(new Float3(0, 1f / root5, 2f / root5), testFloat3.normalised());
    }

    public void testToArray() throws Exception {
        Common.assertEquals(new float[]{testFloat3.getX(), testFloat3.getY(), testFloat3.getZ()}, testFloat3.toArray());
        Common.assertEquals(new float[]{otherFloat3.getX(), otherFloat3.getY(), otherFloat3.getZ()}, otherFloat3.toArray());
    }

    public void testClone() throws Exception {
        assertEquals(testFloat3, testFloat3.clone());
        assertEquals(otherFloat3, otherFloat3.clone());
    }
}
