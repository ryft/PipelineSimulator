package uk.co.ryft.pipeline;

import java.util.ArrayList;
import java.util.Random;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.lighting.LightingModel;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import uk.co.ryft.pipeline.ui.pipeline.PipelineActivity;
import uk.co.ryft.pipeline.ui.setup.SetupCameraActivity;
import uk.co.ryft.pipeline.ui.setup.SetupSceneActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLES20;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SetupActivity extends Activity {

    // Activity request codes
    protected static final int REQUEST_STEP_SCENE = 2;
    protected static final int REQUEST_STEP_CAMERA = 3;

    // Global state set by step configuration
    // Scene composition
    protected ArrayList<Element> mSceneElements = new ArrayList<Element>();
    // Camera parameters
    protected Camera mCamera = new Camera(new Float3(-1.5f, 1, 0), new Float3(1, -0.5f, 0), new Float3(0, 1, 0), -0.5f, 0.5f,
            -0.5f, 0.5f, 1, 3);
    // Lighting model
    protected LightingModel mPreviewLightingModel = LightingModel.PHONG;
    // Face culling
    protected boolean mCullingClockwise = false;
    // TODO Allow choice of depth buffer test using glDepthFunc
    // See http://www.opengl.org/sdk/docs/man/xhtml/glDepthFunc.xml

    ViewHolder steps = new ViewHolder();

    static class ViewHolder {
        View sceneComposition;
        View cameraParameters;
        View lightingModel;
        View vertexProcessing;
        View vertexShading;
        View clipping;
        View multisampling;
        View faceCulling;
        View fragmentShading;
        View depthBufferTest;
        View blending;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // TODO return from saved instance state or extras bundle

        // Find all views associated with individual pipeline steps
        steps.sceneComposition = findViewById(R.id.step_scene_composition);
        steps.cameraParameters = findViewById(R.id.step_camera_parameters);
        steps.lightingModel = findViewById(R.id.step_lighting_model);
        steps.vertexProcessing = findViewById(R.id.step_vertex_processing);
        steps.vertexShading = findViewById(R.id.step_vertex_shading);
        steps.clipping = findViewById(R.id.step_clipping);
        steps.multisampling = findViewById(R.id.step_multisampling);
        steps.faceCulling = findViewById(R.id.step_face_culling);
        steps.fragmentShading = findViewById(R.id.step_fragment_shading);
        steps.depthBufferTest = findViewById(R.id.step_depth_buffer_test);
        steps.blending = findViewById(R.id.step_blending);

        // Put some interesting things in the scene for testing purposes
        Random r = new Random();
        for (int i = 0; i < 64; i++) {
            Element e = ShapeFactory.buildCuboid(
                    new Float3(r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1), r.nextFloat() / 5,
                    r.nextFloat() / 5, r.nextFloat() / 5, Colour.RANDOM, Colour.RANDOM);
            mSceneElements.add(e);
        }

        initialiseViews();
    }

    // XXX To be called once, sets up listeners
    private void initialiseViews() {

        setText(steps.sceneComposition, android.R.id.title, R.string.button_scene_composition);
        setText(steps.cameraParameters, android.R.id.title, R.string.button_camera_parameters);
        setText(steps.lightingModel, android.R.id.title, R.string.button_lighting_model);
        setText(steps.vertexProcessing, android.R.id.title, R.string.button_vertex_processing);
        setText(steps.vertexShading, android.R.id.title, R.string.button_vertex_shading);
        setText(steps.clipping, android.R.id.title, R.string.button_clipping);
        setText(steps.multisampling, android.R.id.title, R.string.button_multisampling);
        setText(steps.faceCulling, android.R.id.title, R.string.button_face_culling);
        setText(steps.fragmentShading, android.R.id.title, R.string.button_fragment_shading);
        setText(steps.depthBufferTest, android.R.id.title, R.string.button_depth_buffer_test);
        setText(steps.blending, android.R.id.title, R.string.button_blending);

        steps.sceneComposition.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Do this for all items
                // Change to startActivityForResult with suitable intent and request code
                // Handle callback on activity result
                Intent intent = new Intent(SetupActivity.this, SetupSceneActivity.class);
                intent.putExtra("elements", mSceneElements);
                startActivityForResult(intent, REQUEST_STEP_SCENE);
            }
        });

        steps.cameraParameters.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, SetupCameraActivity.class);
                intent.putExtra("camera", mCamera);
                startActivityForResult(intent, REQUEST_STEP_CAMERA);
            }
        });

        steps.lightingModel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate and display a configuration dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_lighting_model);
                final LightingModel.Model[] models = LightingModel.Model.values();
                CharSequence[] modelNames = new CharSequence[models.length];
                for (int i = 0; i < models.length; i++)
                    modelNames[i] = models[i].getTitle();
                builder.setItems(modelNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPreviewLightingModel = LightingModel.getLightingModel(models[which]);
                        updateViews();
                    }
                });
                AlertDialog dialogue = builder.create();
                dialogue.show();
            }
        });

        TextView titleVertexProcessing = (TextView) steps.vertexProcessing.findViewById(android.R.id.title);
        titleVertexProcessing.setEnabled(false);

        steps.vertexShading.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);

                View view = SetupActivity.this.getLayoutInflater().inflate(R.layout.component_shader_dialogue, null);
                builder.setView(view);

                TextView messageView = (TextView) view.findViewById(android.R.id.message);
                messageView.setText(mPreviewLightingModel.getVertexShader(GLES20.GL_TRIANGLES));
                messageView.setTextIsSelectable(true);
                messageView.setHorizontallyScrolling(true);

                builder.setTitle(R.string.dialogue_title_vertex_shading);
                builder.setPositiveButton(R.string.dialogue_button_close, null);
                builder.show();
            }

        });

        TextView titleClipping = (TextView) steps.clipping.findViewById(android.R.id.title);
        titleClipping.setEnabled(false);

        steps.faceCulling.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate and display a configuration dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_face_culling);
                builder.setItems(new CharSequence[] { "Wind faces clockwise", "Wind faces counter-clockwise" },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCullingClockwise = (which == 0);
                                updateViews();
                            }
                        });
                AlertDialog dialogue = builder.create();
                dialogue.show();
            }
        });

        steps.fragmentShading.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);

                View view = SetupActivity.this.getLayoutInflater().inflate(R.layout.component_shader_dialogue, null);
                builder.setView(view);

                TextView messageView = (TextView) view.findViewById(android.R.id.message);
                messageView.setText(mPreviewLightingModel.getFragmentShader(GLES20.GL_TRIANGLES));
                messageView.setTextIsSelectable(true);
                messageView.setHorizontallyScrolling(true);

                builder.setTitle(R.string.dialogue_title_fragment_shading);
                builder.setPositiveButton(R.string.dialogue_button_close, null);
                builder.show();
            }

        });

        TextView titleDepthBuffer = (TextView) steps.depthBufferTest.findViewById(android.R.id.title);
        titleDepthBuffer.setEnabled(false);

        TextView titleBlending = (TextView) steps.blending.findViewById(android.R.id.title);
        titleBlending.setEnabled(false);

        Button buttonExit = (Button) findViewById(R.id.button_row_negative);
        buttonExit.setText(R.string.action_button_exit);
        buttonExit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button buttonSimulate = (Button) findViewById(R.id.button_row_positive);
        buttonSimulate.setText(R.string.action_button_simulate);
        buttonSimulate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, PipelineActivity.class);
                intent.putExtra("elements", mSceneElements);
                intent.putExtra("camera", mCamera);
                intent.putExtra("culling_clockwise", mCullingClockwise);
                startActivity(intent);
            }
        });

        updateViews();
    }

    private void updateViews() {
    
        // Generate scene composition summary
        String sceneCompositionSummary = mSceneElements.size() + " element";
        if (mSceneElements.size() != 1)
            sceneCompositionSummary += "s";
    
        // Generate camera parameters summary
        String cameraParametersSummary = "Eye point " + mCamera.getEye() + ", focus point " + mCamera.getFocus() + ".";
    
        // Generate lighting model summary
        String lightingModelSummary = mPreviewLightingModel.toString();
    
        // Generate vertex processing summary
        String vertexProcessingSummary;
        int primitiveCount = 0;
        for (Element e : mSceneElements)
            primitiveCount += e.getPrimitiveCount();
        int vertexCount = 0;
        for (Element e : mSceneElements)
            vertexCount += e.getVertexCount();
    
        vertexProcessingSummary = primitiveCount + " primitive";
        if (primitiveCount != 1)
            vertexProcessingSummary += "s";
        vertexProcessingSummary += " (" + vertexCount;
        if (vertexCount == 1)
            vertexProcessingSummary += " vertex)";
        else
            vertexProcessingSummary += " vertices)";
    
        // Generate vertex shading summary
        String vertexShadingSummary = "Undefined vertex shader";
        switch (mPreviewLightingModel.getModel()) {
            case UNIFORM:
                vertexShadingSummary = "Project vertices into eye space";
                break;
            case LAMBERTIAN:
                vertexShadingSummary = "Calculate diffuse light intensity using normal direction";
                break;
            case PHONG:
                vertexShadingSummary = "Project normal direction for use in fragment shader";
                break;
            case POINT_SOURCE:
                vertexShadingSummary = "Project vertices into eye space and fix a preset size";
                break;
        }
    
        // Generate multisampling summary
        String multisamplingSummary = "TODO";
    
        // Generate face culling summary
        String cullingSummary;
        if (!mCullingClockwise)
            cullingSummary = "Counter-clockwise face winding";
        else
            cullingSummary = "Clockwise face winding";
    
        // Generate fragment shading summary
        String fragmentShadingSummary = "Undefined fragment shader";
        switch (mPreviewLightingModel.getModel()) {
            case UNIFORM:
                fragmentShadingSummary = "Apply a per-primitive uniform light level";
                break;
            case LAMBERTIAN:
                fragmentShadingSummary = "Apply the Gouraud interpolated vertex colour";
                break;
            case PHONG:
                fragmentShadingSummary = "Use interpolated normal to calculate per-pixel intensity";
                break;
            case POINT_SOURCE:
                fragmentShadingSummary = "Apply white colour regardless of light source position";
                break;
        }
    
        // Generate depth buffer summary
        String depthBufferSummary = "TODO";
    
        // Generate blending summary
        String blendingSummary = "TODO";
    
        // TODO Update these properly
    
        setText(steps.sceneComposition, android.R.id.summary, sceneCompositionSummary);
        setText(steps.cameraParameters, android.R.id.summary, cameraParametersSummary);
        setText(steps.lightingModel, android.R.id.summary, lightingModelSummary);
        setText(steps.vertexProcessing, android.R.id.summary, vertexProcessingSummary);
        setText(steps.vertexShading, android.R.id.summary, vertexShadingSummary);
        setText(steps.clipping, android.R.id.summary, R.string.label_clipping);
        setText(steps.multisampling, android.R.id.summary, multisamplingSummary);
        setText(steps.faceCulling, android.R.id.summary, cullingSummary);
        setText(steps.fragmentShading, android.R.id.summary, fragmentShadingSummary);
        setText(steps.depthBufferTest, android.R.id.summary, depthBufferSummary);
        setText(steps.blending, android.R.id.summary, blendingSummary);
    
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_primitive_help:
                View showcasedView = steps.vertexProcessing;
                ViewTarget target = new ViewTarget(showcasedView);
                ShowcaseView.insertShowcaseView(target, this, "Title", "Description");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_STEP_SCENE:

                if (resultCode == RESULT_OK)
                    mSceneElements = ((ArrayList<Element>) data.getSerializableExtra("elements"));
                break;

            case REQUEST_STEP_CAMERA:

                if (resultCode == RESULT_OK)
                    mCamera = (Camera) data.getSerializableExtra("camera");
                break;

            default:
                // Add any new result codes here.
                throw new UnsupportedOperationException();
        }

        updateViews();

    }

    private void setText(View parent, int textViewId, String text) {
        TextView textView = (TextView) parent.findViewById(textViewId);
        textView.setText(text);
    }

    private void setText(View parent, int textViewId, int textId) {
        TextView textView = (TextView) parent.findViewById(textViewId);
        textView.setText(textId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setup, menu);
        return true;
    }

}
