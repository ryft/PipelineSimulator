package uk.co.ryft.pipeline.model.shapes;

import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;

// XXX Not an actual abstract factory but a collection of factory methods
public class ShapeFactory {

    // XXX Regular polygons are created with the normal pointing along the negative z-axis
    public static Primitive buildRegularPolygon(int vertexCount, boolean reverse, Float3 centre, float radius, float rotation,
            Colour colour) {

        List<Float3> points = new LinkedList<Float3>();

        double step = 2 * Math.PI / vertexCount;
        float angle = rotation;

        for (int i = 0; i < vertexCount; i++) {
            float x = (float) (radius * Math.cos(angle) + rotation) + centre.getX();
            float y = (float) (radius * Math.sin(angle) + rotation) + centre.getY();
            points.add(new Float3(x, y, centre.getZ()));

            if (reverse)
                angle += step;
            else
                angle -= step;
        }

        return buildConvexPolygon(points, colour);
    }

    // XXX Builds a right circular cylinder with 'centre' as the centre of the base,
    // extending along the negative z-axis
    public static Composite buildCylinder(int stepCount, Float3 centre, float height, float radius, float rotation,
            Colour colour, Colour capColour) {

        List<Float3> points = new LinkedList<Float3>();

        float angle = rotation;
        float bottom = centre.getZ();
        float top = centre.getZ() - height;
        double step = 2 * Math.PI / stepCount;

        // Note the '<=' to complete the ring of triangles
        for (int i = 0; i <= stepCount; i++) {
            float x = (float) (Math.cos(angle) * radius + centre.getX());
            float y = (float) (Math.sin(angle) * radius + centre.getY());

            points.add(new Float3(x, y, bottom));
            points.add(new Float3(x, y, top));

            // To wind correctly, we need to iterate anti-clockwise around the cylinder
            angle += step;
        }

        List<Primitive> cylinder = new LinkedList<Primitive>();
        Float3 capCentre = new Float3(centre.getX(), centre.getY(), centre.getZ() - height);

        cylinder.add(buildRegularPolygon(stepCount, true, centre, radius, rotation, capColour));
        cylinder.add(buildRegularPolygon(stepCount, false, capCentre, radius, rotation, capColour));
        cylinder.add(new Primitive(Primitive.Type.GL_TRIANGLE_STRIP, points, colour));

        return new Composite(Composite.Type.CYLINDER, cylinder);
    }

    public static Composite buildCuboid(Float3 centre, float width, float height, float depth, Colour frontColour,
            Colour sideColour) {

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

        List<Float3> back = new LinkedList<Float3>();
        back.add(new Float3(x1, y1, z0));
        back.add(new Float3(x1, y0, z0));
        back.add(new Float3(x0, y0, z0));
        back.add(new Float3(x0, y1, z0));
        faces.add(buildConvexPolygon(back, frontColour));

        List<Float3> front = new LinkedList<Float3>();
        front.add(new Float3(x1, y1, z1));
        front.add(new Float3(x0, y1, z1));
        front.add(new Float3(x0, y0, z1));
        front.add(new Float3(x1, y0, z1));
        faces.add(buildConvexPolygon(front, frontColour));

        List<Float3> right = new LinkedList<Float3>();
        right.add(new Float3(x0, y1, z1));
        right.add(new Float3(x0, y1, z0));
        right.add(new Float3(x0, y0, z0));
        right.add(new Float3(x0, y0, z1));
        faces.add(buildConvexPolygon(right, sideColour));

        List<Float3> left = new LinkedList<Float3>();
        left.add(new Float3(x1, y1, z0));
        left.add(new Float3(x1, y1, z1));
        left.add(new Float3(x1, y0, z1));
        left.add(new Float3(x1, y0, z0));
        faces.add(buildConvexPolygon(left, sideColour));

        List<Float3> top = new LinkedList<Float3>();
        top.add(new Float3(x0, y1, z0));
        top.add(new Float3(x0, y1, z1));
        top.add(new Float3(x1, y1, z1));
        top.add(new Float3(x1, y1, z0));
        faces.add(buildConvexPolygon(top, sideColour));

        List<Float3> bottom = new LinkedList<Float3>();
        bottom.add(new Float3(x0, y0, z0));
        bottom.add(new Float3(x1, y0, z0));
        bottom.add(new Float3(x1, y0, z1));
        bottom.add(new Float3(x0, y0, z1));
        faces.add(buildConvexPolygon(bottom, sideColour));

        return new Composite(Composite.Type.CUBOID, faces);
    }

    // XXX Eye is at the origin, focuses along negative Z
    public static Element buildCamera(float scale) {
        return buildCamera(scale, Colour.GREY, Colour.WHITE, Colour.BLACK);
    }

    // XXX Eye is at the origin, focuses along negative Z
    public static Element buildCamera(float scale, Colour bodyColour, Colour lensCasingColour, Colour shutterButtonColour) {

        Float3 origin = new Float3(0, 0, 0);

        // XXX Explain what scale parameter does
        // Scale is the height, other lengths are proportional.
        List<Element> elems = new LinkedList<Element>();
        elems.add(buildCuboid(new Float3(origin.getX(), origin.getY(), origin.getZ() + 0.75f * scale), scale * 2, scale,
                scale / 2, bodyColour, bodyColour));
        elems.add(buildCylinder(10, new Float3(origin.getX(), origin.getY(), origin.getZ() + scale / 2), scale / 2, scale / 2,
                0, lensCasingColour, bodyColour));
        elems.add(buildCuboid(new Float3(origin.getX() - 0.75f * scale, origin.getY() + 0.5625f * scale, origin.getZ() + 0.75f
                * scale), 0.25f * scale, 0.125f * scale, 0.25f * scale, shutterButtonColour, shutterButtonColour));

        return new Composite(Composite.Type.CAMERA, elems);
    }

    // XXX Eye is at the origin, extends along negative Z
    // Ignores camera position
    public static Composite buildFrustum(Camera camera) {

        List<Primitive> frustum = new LinkedList<Primitive>();

        // Enclosing lines, clockwise from top-right
        List<Float3> enclosure = new LinkedList<Float3>();

        float left = camera.getLeft();
        float right = camera.getRight();
        float bottom = camera.getBottom();
        float top = camera.getTop();
        float near = camera.getNear();
        float far = camera.getFar();

        float leftFar = (left / near) * far;
        float rightFar = (right / near) * far;
        float bottomFar = (bottom / near) * far;
        float topFar = (top / near) * far;

        Float3 origin = new Float3(0, 0, 0);

        // XXX Do we need to clone the origin here?
        // XXX Decide whether to clone objects when being passed or received
        enclosure.add((Float3) origin.clone());
        enclosure.add(new Float3(rightFar, topFar, -far));
        enclosure.add((Float3) origin.clone());
        enclosure.add(new Float3(rightFar, bottomFar, -far));
        enclosure.add((Float3) origin.clone());
        enclosure.add(new Float3(leftFar, bottomFar, -far));
        enclosure.add((Float3) origin.clone());
        enclosure.add(new Float3(leftFar, topFar, -far));

        // Viewing plane outlines, near then far
        List<Float3> planeNear = new LinkedList<Float3>();
        planeNear.add(new Float3(right, top, -near));
        planeNear.add(new Float3(right, bottom, -near));
        planeNear.add(new Float3(left, bottom, -near));
        planeNear.add(new Float3(left, top, -near));

        List<Float3> planeFar = new LinkedList<Float3>();
        planeFar.add(new Float3(rightFar, topFar, -far));
        planeFar.add(new Float3(rightFar, bottomFar, -far));
        planeFar.add(new Float3(leftFar, bottomFar, -far));
        planeFar.add(new Float3(leftFar, topFar, -far));

        frustum.add(new Primitive(Primitive.Type.GL_LINES, enclosure, Colour.WHITE));
        frustum.add(new Primitive(Primitive.Type.GL_LINE_LOOP, planeNear, Colour.WHITE));
        frustum.add(new Primitive(Primitive.Type.GL_LINE_LOOP, planeFar, Colour.WHITE));

        return new Composite(Composite.Type.FRUSTUM, frustum);
    }

    // XXX Points must be provided in correct winding order
    public static Primitive buildConvexPolygon(List<Float3> points, Colour colour) {

        return new Primitive(Primitive.Type.GL_TRIANGLE_FAN, points, colour);
    }

}
