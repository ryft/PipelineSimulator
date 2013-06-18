
package uk.co.ryft.pipeline.model;

import android.graphics.Point;

import java.util.List;

public class Element {

    public static enum Type {
        POINTS, LINES, LINE_LOOP, LINE_STRIP, POLYGONS, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, QUAD, QUAD_STRIP
    };
    
    protected Type mType;
    protected List<Point> mVertices;

    public Element(Type type, List<Point> vertices) {
        mType = type;
        mVertices = vertices;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type mType) {
        this.mType = mType;
    }

    public List<Point> getVertices() {
        return mVertices;
    }

    public void setVertices(List<Point> mVertices) {
        this.mVertices = mVertices;
    }

}
