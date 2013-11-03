
package uk.co.ryft.pipeline.model.shapes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.FloatPoint;

public class ConvexPolygon {

    public static Composite getRegularPolygon(int vertexCount, FloatPoint centre, double radius, double rotation, Colour colour) {       
        
        List<FloatPoint> points = new LinkedList<FloatPoint>();
        
        for (int i = 0; i < vertexCount; i++) {
            float x = (float) (radius * Math.cos(2 * Math.PI * ((double) i / vertexCount) + rotation) + centre.getX());
            float y = (float) (radius * Math.sin(2 * Math.PI * ((double) i / vertexCount) + rotation) + centre.getY());
            float z = centre.getZ();
            points.add(new FloatPoint(x, y, z));
        }
        
        return getConvexPolygon(points, colour);
    }

    public static Composite getConvexPolygon(List<FloatPoint> points, Colour colour) {

        return new Composite(Composite.Type.CONVEX_POLYGON, Collections.singletonList(new Primitive(Primitive.Type.GL_TRIANGLE_FAN, points, colour)));
    }

}
