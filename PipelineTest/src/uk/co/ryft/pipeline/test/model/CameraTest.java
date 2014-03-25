package uk.co.ryft.pipeline.test.model;

import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Float3;
import android.test.InstrumentationTestCase;

public class CameraTest extends InstrumentationTestCase {

    Camera testCamera;
    Camera destCamera;

    protected void setUp() throws Exception {
        super.setUp();
        testCamera = new Camera(new Float3(1, 1, 1), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 1, 2);
        destCamera = new Camera(new Float3(0, 0, 0), new Float3(1, 1, 1), new Float3(1, 0, 1), -1, 1, -1, 1, 1, 2);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testTransformTo() throws Exception {

        testCamera.transformTo(destCamera, 100);

        assertTrue(testCamera.isTransforming());
        Thread.sleep(200);
        assertFalse(testCamera.isTransforming());

        float[] view0 = new float[16];
        float[] view1 = new float[16];
        float[] projection0 = new float[16];
        float[] projection1 = new float[16];

        destCamera.setViewMatrix(view0, 0);
        testCamera.setViewMatrix(view1, 0);
        destCamera.setProjectionMatrix(projection0, 0, 100, 100);
        testCamera.setProjectionMatrix(projection1, 0, 100, 100);

        for (int i = 0; i < 16; i++) {
            assertEquals(view0[i], view1[i], 0.01f);
            assertEquals(projection0[i], projection1[i], 0.01f);
        }
    }

    public void testUpdateScaleFactor() throws Exception {
        fail("Not yet implemented");
    }

    public void testSetViewMatrix() throws Exception {
        fail("Not yet implemented");
    }

    public void testIsTransforming() throws Exception {
        fail("Not yet implemented");
    }

    public void testSetProjectionMatrix() throws Exception {
        fail("Not yet implemented");
    }

    public void testClone() throws Exception {
        Camera cloned = testCamera.clone();
        assertTrue(testCamera.equals(cloned));
    }

}
