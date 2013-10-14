
package uk.co.ryft.pipeline.model;

import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.FloatPoint;
import uk.co.ryft.pipeline.model.Element.Type;

public class ConvexPolygon {
    
    public static Element getRegularPolygon(int vertexCount, FloatPoint centre, double radius, double rotation, Colour colour) {       
        
        List<FloatPoint> points = new LinkedList<FloatPoint>();
        
        for (int i = 0; i < vertexCount; i++) {
            float x = (float) (radius * Math.cos(2 * Math.PI * ((double) i / vertexCount) + rotation) + centre.getX());
            float y = (float) (radius * Math.sin(2 * Math.PI * ((double) i / vertexCount) + rotation) + centre.getY());
            float z = centre.getZ();
            points.add(new FloatPoint(x, y, z));
        }
        
        return getConvexPolygon(points, colour);
    }

    public static Element getConvexPolygon(List<FloatPoint> points, Colour colour) {

        return new Element(Type.GL_TRIANGLE_FAN, points, colour);
    }

}
