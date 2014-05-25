package uk.co.ryft.pipeline.test;

import junit.framework.Assert;

import uk.co.ryft.pipeline.model.Float3;

public class Common {

    /**
     * Custom assertion for float arrays
     *
     * @param expected Expected array
     * @param actual   Actual array
     * @param delta    Allowed margin of error for each float
     */
    public static void assertEquals(float[] expected, float[] actual, float delta) {
        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(expected[i], actual[i], delta);
    }

    /**
     * Custom assertion for float arrays with a margin of error of 0.00001
     *
     * @param expected Expected array
     * @param actual   Actual array
     */
    public static void assertEquals(float[] expected, float[] actual) {
        assertEquals(expected, actual, 0.00001f);
    }

    /**
     * Custom assertion for Float3s
     *
     * @param expected Expected Float3
     * @param actual   Actual Float3
     * @param delta    Allowed margin of error for each coordinate
     */
    public static void assertEquals(Float3 expected, Float3 actual, float delta) {
        Assert.assertEquals(expected.getX(), actual.getX(), delta);
        Assert.assertEquals(expected.getY(), actual.getY(), delta);
        Assert.assertEquals(expected.getZ(), actual.getZ(), delta);
    }

    /**
     * Custom assertion for Float3s with a margin of error of 0.00001
     *
     * @param expected Expected Float3
     * @param actual   Actual Float3
     */
    public static void assertEquals(Float3 expected, Float3 actual) {
        assertEquals(expected, actual, 0.00001f);
    }

}
