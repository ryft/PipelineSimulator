
package uk.co.ryft.pipeline.model;

import uk.co.ryft.pipeline.gl.FloatPoint;

import java.io.Serializable;
import java.util.List;

public class Element implements Comparable<Element>, Serializable {

    private static final long serialVersionUID = 5661009688352125290L;

    public static enum Type {
        POINTS, LINES, LINE_LOOP, LINE_STRIP, POLYGONS, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUAD, QUAD_STRIP
    };
    
    protected Type mType;
    protected List<FloatPoint> mVertices;

    public Element(Type type, List<FloatPoint> vertices) {
        mType = type;
        mVertices = vertices;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type mType) {
        this.mType = mType;
    }

    public List<FloatPoint> getVertices() {
        return mVertices;
    }

    public void setVertices(List<FloatPoint> mVertices) {
        this.mVertices = mVertices;
    }

    @Override
    public int compareTo(Element arg0) {
        // Compare z-orders. TODO: cache these min/max values for faster sorting.
        return 0;
    }

}
