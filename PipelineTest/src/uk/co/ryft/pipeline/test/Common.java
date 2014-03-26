package uk.co.ryft.pipeline.test;

import junit.framework.Assert;

import uk.co.ryft.pipeline.model.Float3;

public class Common {

    // Custom assertion for float arrays
    public static void assertEquals(float[] expected, float[] actual, float delta) {
        Assert.assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            Assert.assertEquals(expected[i], actual[i], delta);
    }

    // Custom assertion for float arrays
    public static void assertEquals(float[] expected, float[] actual) {
        assertEquals(expected, actual, 0.00001f);
    }

    // Custom assertion for Float3s
    public static void assertEquals(Float3 expected, Float3 actual, float delta) {
        Assert.assertEquals(expected.getX(), actual.getX(), delta);
        Assert.assertEquals(expected.getY(), actual.getY(), delta);
        Assert.assertEquals(expected.getZ(), actual.getZ(), delta);
    }

    // Custom assertion for Float3s
    public static void assertEquals(Float3 expected, Float3 actual) {
        assertEquals(expected, actual, 0.00001f);
    }

}
