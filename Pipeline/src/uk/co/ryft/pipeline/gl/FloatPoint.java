package uk.co.ryft.pipeline.gl;

import java.io.Serializable;

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
    
    @Override
    public String toString() {
        return "("+getX()+", "+getY()+", "+getZ()+")";
    }

}
