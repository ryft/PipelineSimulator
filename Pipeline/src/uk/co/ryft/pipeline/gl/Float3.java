package uk.co.ryft.pipeline.gl;

import java.io.Serializable;

import android.opengl.Matrix;

public class Float3 implements Serializable, Cloneable {

    private static final long serialVersionUID = -4166884951552173806L;

    private float x;
    private float y;
    private float z;

    public Float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    // XXX shortcut
    public void setCoordinates(float x, float y, float z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public void transform(float[] transformation) {

        float[] v = new float[] { this.x, this.y, this.z, 1 };
        Matrix.multiplyMV(v, 0, transformation, 0, v, 0);
        setCoordinates(v[0], v[1], v[2]);
    }

    // XXX Find out whether or not using Matrix operations is better than a home-grown solution.
    // Perhaps run a test to see how long matrix ops really take.
    // TODO: Try to multiply multiple points in one M x V operation in Primitive.
    // This function belongs in Element, not Point.
    // XXX This rotates about the origin.
    public void rotate(float a, float x, float y, float z) {

        float[] v = new float[] { this.x, this.y, this.z, 1 };

        float[] m = new float[16];
        Matrix.setIdentityM(m, 0);
        Matrix.rotateM(m, 0, a, x, y, z);
        Matrix.multiplyMV(v, 0, m, 0, v, 0);

        setCoordinates(v[0], v[1], v[2]);
    }

    public void translate(float x, float y, float z) {
        setX(getX() + x);
        setY(getY() + y);
        setZ(getZ() + z);
    }

    /* Modifications which return new objects */
    public Float3 plus(Float3 v) {
        return new Float3(getX() + v.getX(), getY() + v.getY(), getZ() + v.getZ());
    }

    public Float3 minus(Float3 v) {
        return new Float3(getX() - v.getX(), getY() - v.getY(), getZ() - v.getZ());
    }

    public Float3 scale(float sf) {
        return new Float3(getX() * sf, getY() * sf, getZ() * sf);
    }

    public float dot(Float3 v) {
        return (getX() * v.getX()) + (getY() * v.getY()) + (getZ() * v.getZ());
    }

    public Float3 cross(Float3 v) {
        float x = getY() * v.getZ() - getZ() * v.getY();
        float y = getZ() * v.getX() - getX() * v.getZ();
        float z = getX() * v.getY() - getY() * v.getX();

        return new Float3(x, y, z);
    }

    public Float3 normalised() {
        float norm = (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
        return new Float3(getX() / norm, getY() / norm, getZ() / norm);
    }

    public float[] toArray() {
        float[] a = { x, y, z };
        return a;
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }

    @Override
    public Object clone() {
        return new Float3(x, y, z);
    }

}
