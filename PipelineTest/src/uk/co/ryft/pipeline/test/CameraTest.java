package uk.co.ryft.pipeline.test;

import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.PipelineRenderer;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.util.FloatMath;
import android.util.Log;

import java.util.Random;

public class CameraTest extends AndroidTestCase {

    Camera testCamera;
    Camera destCamera;
    float delta = 0.00001f;

    protected void setUp() throws Exception {
        super.setUp();
        testCamera = new Camera(new Float3(1, 1, 1), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 1, 2);
        destCamera = new Camera(new Float3(0, 0, 0), new Float3(1, 1, 1), new Float3(1, 0, 1), -1, 1, -1, 1, 1, 2);
    }

    public void testTransformTo() throws Exception {

        // Start transformation
        testCamera.transformTo(destCamera, 100);

        // Test transformTo() method during and after transformation
        assertTrue(testCamera.isTransforming());
        Thread.sleep(200);
        assertFalse(testCamera.isTransforming());

        float[] view0 = new float[16];
        float[] view1 = new float[16];
        float[] projection0 = new float[16];
        float[] projection1 = new float[16];

        // Generated view and projection matrices
        destCamera.setViewMatrix(view0, 0);
        testCamera.setViewMatrix(view1, 0);
        destCamera.setProjectionMatrix(projection0, 0, 100, 100);
        testCamera.setProjectionMatrix(projection1, 0, 100, 100);

        // Compare generated matrices
        assertEquals(view0, view1, delta);
        assertEquals(projection0, projection1, delta);

        // Reset camera ready for next tests
        testCamera.setRotation(0);
        testCamera.setScaleFactor(1);
    }

    public void testUpdateScaleFactor() throws Exception {

        Random r = new Random();
        float scaleFactor = 1;

        // Scale by a random factor 100 times
        for (int i = 0; i < 100; i++) {
            float factor = r.nextFloat() * 2;
            scaleFactor /= factor;
            testCamera.updateScaleFactor(factor);
        }

        // Check the scale factor is correct
        assertEquals(scaleFactor, testCamera.getScaleFactor());
    }

    public void testSetViewMatrix() throws Exception {

        // Generate view matrix for the test camera
        float[] view = new float[16];
        testCamera.setViewMatrix(view, 0);

        // Left vector = (1, 0 -1)
        float[] leftExpected = new Float3(1, 0, -1).normalised().toArray();
        float[] leftActual = new float[]{view[0], view[4], view[8]};
        assertEquals(leftExpected, leftActual, delta);

        // Up vector = (-1, 2, -1)
        float[] upExpected = new Float3(-1, 2, -1).normalised().toArray();
        float[] upActual = new float[]{view[1], view[5], view[9]};
        assertEquals(upExpected, upActual, delta);

        // Forward vector = (1, 1, 1)
        float[] forwardExpected = new Float3(1, 1, 1).normalised().toArray();
        float[] forwardActual = new float[]{view[2], view[6], view[10]};
        assertEquals(forwardExpected, forwardActual, delta);

        // Translation = (0, 0, sqrt(3))
        float[] transExpected = new Float3(0, 0, -FloatMath.sqrt(3)).toArray();
        float[] transActual = new float[]{view[12], view[13], view[14]};
        assertEquals(transExpected, transActual, delta);

        // Homogeneous coordinates = (0, 0, 0, 1)
        float[] homogeneousExpected = new float[]{0, 0, 0, 1};
        float[] homogeneousActual = new float[]{view[3], view[7], view[11], view[15]};
        assertEquals(homogeneousExpected, homogeneousActual, delta);
    }

    public void testSetProjectionMatrix() throws Exception {

        // Generate projection matrix
        float[] projection = new float[16];
        testCamera.setProjectionMatrix(projection, 0, 100, 100);

        float near = testCamera.getNear();
        float far = testCamera.getFar();

        // Test projection correctness
        // m[0] = near / right
        assertEquals(near / testCamera.getRight(), projection[0], delta);
        // m[5] = near / top
        assertEquals(near / testCamera.getTop(), projection[5], delta);
        // m[10] = -(f + n) / (f - n)
        assertEquals(-(far + near) / (far - near), projection[10], delta);
        // m[11] = -1
        assertEquals(-1.0, projection[11], delta);
        // m[14] = -2fn / (f - n)
        assertEquals(-(2 * far * near) / (far - near), projection[14], delta);
    }

    public void testClone() throws Exception {
        Camera cloned = testCamera.clone();
        assertTrue(testCamera.equals(cloned));
    }

    // Custom assertion for float arrays
    private void assertEquals(float[] expected, float[] actual, float delta) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], actual[i], delta);
    }

}
