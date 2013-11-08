package uk.co.ryft.pipeline.model.shapes;

import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.FloatPoint;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.Primitive.Type;

// XXX Not an actual abstract factory but a collection of factory methods
public class ShapeFactory {

    // XXX Regular polygons are created with the normal pointing along the positive z-axis
    public static Primitive buildRegularPolygon(int vertexCount, boolean reverse,
            FloatPoint centre, float radius, float rotation, Colour colour) {

        List<FloatPoint> points = new LinkedList<FloatPoint>();

        double step = 2 * Math.PI / vertexCount;
        float angle = rotation;

        for (int i = 0; i < vertexCount; i++) {
            float x = (float) (radius * Math.cos(angle) + rotation) + centre.getX();
            float y = (float) (radius * Math.sin(angle) + rotation) + centre.getY();
            points.add(new FloatPoint(x, y, centre.getZ()));

            if (reverse)
                angle -= step;
            else
                angle += step;
        }

        return buildConvexPolygon(points, colour);
    }

    // XXX Builds a right circular cylinder with 'centre' as the centre of the base,
    // extending along the positive z-axis
    public static Composite buildCylinder(int stepCount, FloatPoint centre, float height,
            float radius, float rotation, Colour colour, Colour capColour) {

        List<FloatPoint> points = new LinkedList<FloatPoint>();

        float angle = rotation;
        float bottom = centre.getZ();
        float top = centre.getZ() + height;
        double step = 2 * Math.PI / stepCount;

        // Note the '<=' to complete the ring of triangles
        for (int i = 0; i <= stepCount; i++) {
            float x = (float) (Math.cos(angle) * radius + centre.getX());
            float y = (float) (Math.sin(angle) * radius + centre.getY());

            points.add(new FloatPoint(x, y, bottom));
            points.add(new FloatPoint(x, y, top));

            // To wind correctly, we need to iterate anti-clockwise around the cylinder
            angle -= step;
        }

        List<Primitive> cylinder = new LinkedList<Primitive>();
        FloatPoint capCentre = new FloatPoint(centre.getX(), centre.getY(), centre.getZ() + height);

        cylinder.add(buildRegularPolygon(stepCount, true, centre, radius, rotation, capColour));
        cylinder.add(buildRegularPolygon(stepCount, false, capCentre, radius, rotation, capColour));
        cylinder.add(new Primitive(Type.GL_TRIANGLE_STRIP, points, colour));

        return new Composite(Composite.Type.CUSTOM_SHAPE, cylinder);
    }

    public static Composite buildCuboid(FloatPoint centre, float width, float height, float depth,
            Colour colour) {

        float x = centre.getX();
        float y = centre.getY();
        float z = centre.getZ();

        float dx = width / 2;
        float dy = height / 2;
        float dz = depth / 2;

        float x0 = x - dx;
        float x1 = x + dx;
        float y0 = y - dy;
        float y1 = y + dy;
        float z0 = z - dz;
        float z1 = z + dz;

        List<Primitive> faces = new LinkedList<Primitive>();

        List<FloatPoint> back = new LinkedList<FloatPoint>();
        back.add(new FloatPoint(x1, y1, z0));
        back.add(new FloatPoint(x1, y0, z0));
        back.add(new FloatPoint(x0, y0, z0));
        back.add(new FloatPoint(x0, y1, z0));
        faces.add(buildConvexPolygon(back, colour));

        List<FloatPoint> front = new LinkedList<FloatPoint>();
        front.add(new FloatPoint(x1, y1, z1));
        front.add(new FloatPoint(x0, y1, z1));
        front.add(new FloatPoint(x0, y0, z1));
        front.add(new FloatPoint(x1, y0, z1));
        faces.add(buildConvexPolygon(front, colour));

        List<FloatPoint> right = new LinkedList<FloatPoint>();
        right.add(new FloatPoint(x0, y1, z1));
        right.add(new FloatPoint(x0, y1, z0));
        right.add(new FloatPoint(x0, y0, z0));
        right.add(new FloatPoint(x0, y0, z1));
        faces.add(buildConvexPolygon(right, colour));

        List<FloatPoint> left = new LinkedList<FloatPoint>();
        left.add(new FloatPoint(x1, y1, z0));
        left.add(new FloatPoint(x1, y1, z1));
        left.add(new FloatPoint(x1, y0, z1));
        left.add(new FloatPoint(x1, y0, z0));
        faces.add(buildConvexPolygon(left, colour));

        List<FloatPoint> top = new LinkedList<FloatPoint>();
        top.add(new FloatPoint(x0, y1, z0));
        top.add(new FloatPoint(x0, y1, z1));
        top.add(new FloatPoint(x1, y1, z1));
        top.add(new FloatPoint(x1, y1, z0));
        faces.add(buildConvexPolygon(top, colour));

        List<FloatPoint> bottom = new LinkedList<FloatPoint>();
        bottom.add(new FloatPoint(x0, y0, z0));
        bottom.add(new FloatPoint(x1, y0, z0));
        bottom.add(new FloatPoint(x1, y0, z1));
        bottom.add(new FloatPoint(x0, y0, z1));
        faces.add(buildConvexPolygon(bottom, colour));

        return new Composite(Composite.Type.CUSTOM_SHAPE, faces);
    }

    public static Element buildCamera(FloatPoint eye, float scale) {

        // XXX Explain what scale parameter does
        List<Element> camera = new LinkedList<Element>();
        camera.add(buildCuboid(new FloatPoint(eye.getX(), eye.getY(), eye.getZ() - 0.75f * scale),
                scale * 2, scale, scale / 2, Colour.GREY));
        camera.add(buildCylinder(16, new FloatPoint(0, 0, -scale / 2), scale / 2, scale / 2, 0,
                Colour.WHITE, Colour.GREY));
        camera.add(buildCuboid(new FloatPoint(eye.getX() - 0.75f * scale, eye.getY() + 0.5625f
                * scale, eye.getZ() - 0.75f * scale), 0.25f * scale, 0.125f * scale, 0.25f * scale,
                Colour.BLACK));

        return new Composite(Composite.Type.CUSTOM_SHAPE, camera);
    }

    // XXX Points must be provided in correct winding order
    public static Primitive buildConvexPolygon(List<FloatPoint> points, Colour colour) {

        return new Primitive(Type.GL_TRIANGLE_FAN, points, colour);
    }

}
