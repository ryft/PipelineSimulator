package uk.co.ryft.pipeline.gl;

import java.io.Serializable;

import android.opengl.Matrix;

public class FloatPoint implements Serializable {
    
    private static final long serialVersionUID = -4166884951552173806L;
    
    private float x;
    private float y;
    private float z;
    
    public FloatPoint(float x, float y, float z) {
        super();
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
        setX(x); setY(y); setZ(z);
    }

    // XXX Find out whether or not using Matrix operations is better than a home-grown solution.
    // Perhaps run a test to see how long matrix ops really take.
    // XXX This rotates about the origin.
    public void rotate(float a, float x, float y, float z) {
        
        float[] v = new float[] {this.x, this.y, this.z, 1};
        
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
    
    @Override
    public String toString() {
        return "("+getX()+", "+getY()+", "+getZ()+")";
    }

}
