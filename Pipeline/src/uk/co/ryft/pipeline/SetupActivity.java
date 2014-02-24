package uk.co.ryft.pipeline;

import java.util.ArrayList;
import java.util.Random;

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
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.SimpleShowcaseEventListener;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ViewTarget;

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

    SharedPreferences prefs = null;

    ViewHolder steps = new ViewHolder();

    static class ViewHolder {
        View groupSceneDefinition;
        View groupVertexProcessing;
        View groupPrimitiveProcessing;
        View groupRasterisation;
        View groupFragmentProcessing;
        View groupPixelProcessing;

        View stepSceneComposition;
        View stepCameraParameters;
        View stepLightingModel;
        View stepVertexAssembly;
        View stepVertexShading;
        View stepClipping;
        View stepMultisampling;
        View stepFaceCulling;
        View stepFragmentShading;
        View stepDepthBufferTest;
        View stepBlending;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // TODO return from saved instance state or extras bundle

        // Find all views associated with individual pipeline steps
        steps.groupSceneDefinition = findViewById(R.id.group_scene_definition);
        steps.groupVertexProcessing = findViewById(R.id.group_vertex_processing);
        steps.groupPrimitiveProcessing = findViewById(R.id.group_primitive_processing);
        steps.groupRasterisation = findViewById(R.id.group_rasterisation);
        steps.groupFragmentProcessing = findViewById(R.id.group_fragment_processing);
        steps.groupPixelProcessing = findViewById(R.id.group_pixel_processing);

        steps.stepSceneComposition = findViewById(R.id.step_scene_composition);
        steps.stepCameraParameters = findViewById(R.id.step_camera_parameters);
        steps.stepLightingModel = findViewById(R.id.step_lighting_model);
        steps.stepVertexAssembly = findViewById(R.id.step_vertex_assembly);
        steps.stepVertexShading = findViewById(R.id.step_vertex_shading);
        steps.stepClipping = findViewById(R.id.step_clipping);
        steps.stepMultisampling = findViewById(R.id.step_multisampling);
        steps.stepFaceCulling = findViewById(R.id.step_face_culling);
        steps.stepFragmentShading = findViewById(R.id.step_fragment_shading);
        steps.stepDepthBufferTest = findViewById(R.id.step_depth_buffer_test);
        steps.stepBlending = findViewById(R.id.step_blending);

        // Put some interesting things in the scene for testing purposes
        Random r = new Random();
        for (int i = 0; i < 64; i++) {
            Element e = ShapeFactory.buildCuboid(
                    new Float3(r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1, r.nextFloat() * 2 - 1), r.nextFloat() / 5,
                    r.nextFloat() / 5, r.nextFloat() / 5, Colour.RANDOM, Colour.RANDOM);
            mSceneElements.add(e);
        }

        initialiseViews();

        // Get shared preferences reference for first run detection
        prefs = getSharedPreferences("uk.co.ryft.pipeline", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Show First Run help message if necessary
        if (prefs.getBoolean("firstrun", true)) {
            ShowcaseView.insertShowcaseView(new ActionItemTarget(this, R.id.action_setup_help), this,
                    R.string.help_firstrun_title, R.string.help_firstrun_desc);
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    // XXX To be called once, sets up listeners
    private void initialiseViews() {

        setText(steps.stepSceneComposition, android.R.id.title, R.string.button_scene_composition);
        setText(steps.stepCameraParameters, android.R.id.title, R.string.button_camera_parameters);
        setText(steps.stepLightingModel, android.R.id.title, R.string.button_lighting_model);
        setText(steps.stepVertexAssembly, android.R.id.title, R.string.button_vertex_assembly);
        setText(steps.stepVertexShading, android.R.id.title, R.string.button_vertex_shading);
        setText(steps.stepClipping, android.R.id.title, R.string.button_clipping);
        setText(steps.stepMultisampling, android.R.id.title, R.string.button_multisampling);
        setText(steps.stepFaceCulling, android.R.id.title, R.string.button_face_culling);
        setText(steps.stepFragmentShading, android.R.id.title, R.string.button_fragment_shading);
        setText(steps.stepDepthBufferTest, android.R.id.title, R.string.button_depth_buffer_test);
        setText(steps.stepBlending, android.R.id.title, R.string.button_blending);

        steps.stepSceneComposition.setOnClickListener(new OnClickListener() {
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

        steps.stepCameraParameters.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, SetupCameraActivity.class);
                intent.putExtra("camera", mCamera);
                startActivityForResult(intent, REQUEST_STEP_CAMERA);
            }
        });

        steps.stepLightingModel.setOnClickListener(new OnClickListener() {
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

        TextView titleVertexProcessing = (TextView) steps.stepVertexAssembly.findViewById(android.R.id.title);
        titleVertexProcessing.setEnabled(false);

        steps.stepVertexShading.setOnClickListener(new OnClickListener() {

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

        TextView titleClipping = (TextView) steps.stepClipping.findViewById(android.R.id.title);
        titleClipping.setEnabled(false);

        steps.stepFaceCulling.setOnClickListener(new OnClickListener() {
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

        steps.stepFragmentShading.setOnClickListener(new OnClickListener() {

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

        TextView titleDepthBuffer = (TextView) steps.stepDepthBufferTest.findViewById(android.R.id.title);
        titleDepthBuffer.setEnabled(false);

        TextView titleBlending = (TextView) steps.stepBlending.findViewById(android.R.id.title);
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

        // Generate vertex assembly summary
        String vertexAssemblySummary;
        int primitiveCount = 0;
        for (Element e : mSceneElements)
            primitiveCount += e.getPrimitiveCount();
        int vertexCount = 0;
        for (Element e : mSceneElements)
            vertexCount += e.getVertexCount();

        vertexAssemblySummary = primitiveCount + " primitive";
        if (primitiveCount != 1)
            vertexAssemblySummary += "s";
        vertexAssemblySummary += " (" + vertexCount;
        if (vertexCount == 1)
            vertexAssemblySummary += " vertex)";
        else
            vertexAssemblySummary += " vertices)";

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

        // TODO Update these properly

        // Generate depth buffer summary
        String depthBufferSummary = "TODO";

        // Generate blending summary
        String blendingSummary = "TODO";

        setText(steps.stepSceneComposition, android.R.id.summary, sceneCompositionSummary);
        setText(steps.stepCameraParameters, android.R.id.summary, cameraParametersSummary);
        setText(steps.stepLightingModel, android.R.id.summary, lightingModelSummary);
        setText(steps.stepVertexAssembly, android.R.id.summary, vertexAssemblySummary);
        setText(steps.stepVertexShading, android.R.id.summary, vertexShadingSummary);
        setText(steps.stepClipping, android.R.id.summary, R.string.desc_clipping);
        setText(steps.stepMultisampling, android.R.id.summary, multisamplingSummary);
        setText(steps.stepFaceCulling, android.R.id.summary, cullingSummary);
        setText(steps.stepFragmentShading, android.R.id.summary, fragmentShadingSummary);
        setText(steps.stepDepthBufferTest, android.R.id.summary, depthBufferSummary);
        setText(steps.stepBlending, android.R.id.summary, blendingSummary);

    }

    protected ShowcaseView insertShowcaseView(View target, int title, int description, float scale, ConfigOptions options,
            OnShowcaseEventListener listener) {
        ShowcaseView sv = ShowcaseView.insertShowcaseView(new ViewTarget(target), this, title, description, options);
        sv.setOnShowcaseEventListener(listener);
        sv.setScaleMultiplier(scale);
        return sv;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup_help:
                final ScrollView scroll = (ScrollView) findViewById(R.id.setup_scrollview);

                final OnShowcaseEventListener help4 = new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        LayoutParams lps = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        lps.setMargins(12, 12, 12, 12);
                        ConfigOptions options = new ConfigOptions();
                        options.buttonLayoutParams = lps;
                        insertShowcaseView(findViewById(R.id.button_row_positive), R.string.help_simulate_title,
                                R.string.help_simulate_desc, 0.9f, options, OnShowcaseEventListener.NONE);
                    }
                };

                final OnShowcaseEventListener help3 = new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        scroll.scrollTo(0, (int) steps.stepBlending.getY());
                        insertShowcaseView(steps.stepClipping.findViewById(android.R.id.title), R.string.help_disabled_title,
                                R.string.help_disabled_desc, 1.8f, null, help4);
                    }
                };

                final OnShowcaseEventListener help2 = new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        scroll.smoothScrollTo(0, (int) steps.stepLightingModel.getY());
                        insertShowcaseView(steps.stepLightingModel.findViewById(android.R.id.title),
                                R.string.help_lighting_preview_title, R.string.help_lighting_preview_desc, 1, null, help3);
                    }
                };

                scroll.smoothScrollTo(0, 0);
                insertShowcaseView(steps.stepSceneComposition.findViewById(android.R.id.title),
                        R.string.heading_group_scene_definition, R.string.help_scene_definition_desc, 1.5f, null, help2);

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
