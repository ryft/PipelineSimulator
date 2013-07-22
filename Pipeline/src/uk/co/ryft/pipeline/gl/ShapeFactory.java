
package uk.co.ryft.pipeline.gl;

import uk.co.ryft.pipeline.model.Drawable;
import uk.co.ryft.pipeline.model.Element;

public class ShapeFactory {

    public static Drawable getDrawable(Element e) {

        switch (e.getType()) {
            case GL_POINTS:
                break;
            case GL_LINES:
                break;
            case GL_LINE_STRIP:
                break;
            case GL_LINE_LOOP:
                break;
            case GL_TRIANGLES:
                break;
            case GL_TRIANGLE_STRIP:
                break;
            case GL_TRIANGLE_FAN:
                break;
            case GL_QUADS:
                break;
            case GL_QUAD_STRIP:
                break;
            case GL_POLYGON:
                break;
            default:
                break;
        }

        return null;
    }

}
