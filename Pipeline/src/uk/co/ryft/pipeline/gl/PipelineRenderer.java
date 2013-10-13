package uk.co.ryft.pipeline.gl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import uk.co.ryft.pipeline.model.Drawable;
import uk.co.ryft.pipeline.model.Element;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class PipelineRenderer implements Renderer {

    private static final String TAG = "PipelineRenderer";

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mIdentityMatrix = new float[16];

    private final Map<Element, Drawable> mElements = new LinkedHashMap<Element, Drawable>();
    
    private boolean mTogglePerspective = false;

    public void togglePerspective() {
        mTogglePerspective = !mTogglePerspective;
    }

    // TODO Should these belong here?
    public static final String VERTEX_SHADER_EMPTY =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    "  gl_Position = vPosition * uMVPMatrix;" +
                    "}";

    public static final String FRAGMENT_SHADER_EMPTY =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background colour
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set up an identity matrix
        Matrix.setIdentityM(mIdentityMatrix, 0);

        // Set the camera position (View matrix)
        if (mTogglePerspective)
            Matrix.setLookAtM(mVMatrix, 0, -1f, -1f, 5f, 0f, 0f, 0f, 0f, 1f, 0f);
        else
            Matrix.setLookAtM(mVMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, 0f, 1f, 0f);
        // Params: matrix, offset, eye(x, y, z), focus(x, y, z), up(x, y, z).
        // XXX Coords are flipped on screen - explain why.

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        // Draw objects in the scene
        for (Element e : mElements.keySet()) {
            if (mElements.get(e) == null)
                mElements.put(e, e.getDrawable());
            Drawable d = mElements.get(e);
            if (d != null)
                d.draw(mMVPMatrix);
            else
                System.out.println("Ruh-roh, null drawable!");
        }
        
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        if (width >= height) {
            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        } else {
            float ratio = (float) height / width;
            Matrix.frustumM(mProjMatrix, 0, -1, 1, -ratio, ratio, 3, 7);

        }

    }

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call just after making it:
     * 
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
     * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
     * </pre>
     * 
     * If the operation is not successful, the check throws an error.
     * 
     * @param glOperation
     *            - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void addToScene(Element e) {
        mElements.put(e, null);
    }

    public void updateScene(List<Element> elements) {
        mElements.clear();
        for (Element e : elements)
            mElements.put(e, null);
    }

}
