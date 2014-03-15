package uk.co.ryft.pipeline;

import java.util.ArrayList;
import java.util.Random;

import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.lighting.LightingModel;
import uk.co.ryft.pipeline.gl.lighting.LightingModel.Model;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import uk.co.ryft.pipeline.ui.pipeline.PipelineActivity;
import uk.co.ryft.pipeline.ui.setup.GLConfig;
import uk.co.ryft.pipeline.ui.setup.SetupBlendingActivity;
import uk.co.ryft.pipeline.ui.setup.SetupCameraActivity;
import uk.co.ryft.pipeline.ui.setup.SetupSceneActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.SimpleShowcaseEventListener;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.Target;
import com.espian.showcaseview.targets.ViewTarget;

public class SetupActivity extends Activity {

    // Activity request codes
    protected static final int REQUEST_STEP_SCENE = 2;
    protected static final int REQUEST_STEP_CAMERA = 3;
    protected static final int REQUEST_STEP_BLENDING = 4;

    // Global state set by step configuration
    // Scene composition
    protected ArrayList<Element> mSceneElements = new ArrayList<Element>();
    // Camera parameters
    protected Camera mCamera = new Camera(new Float3(-4, 1, 0), new Float3(0, 0, 0), new Float3(0, 1, 0), -1, 1, -1, 1, 3, 5);
    // Light source position
    protected Float3 mLightPosition = new Float3(-2, 2, -3);
    // Lighting model
    protected LightingModel mPreviewLightingModel = LightingModel.getLightingModel(Model.PHONG);
    // Transition animation duration
    protected int mAnimationDuration = 2000;
    // Multisampling
    protected int mMinSamples = 2;
    // Face culling
    protected boolean mCullingClockwise = false;
    // Depth buffer test function (Default: GL_LESS)
    protected int mDepthFunc = 1;
    // Blending parameters
    protected int mBlendFuncSrc = 2;
    protected int mBlendFuncDst = 3;
    protected int mBlendEquation = 0;

    public static GLConfig<Integer> DepthFunc;
    public static GLConfig<Integer> BlendFunc;
    public static GLConfig<Integer> BlendEquation;

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
        View stepLightPosition;
        View stepLightingModel;
        View stepAnimationDuration;
        View stepVertexAssembly;
        View stepVertexShading;
        View stepClipping;
        View stepMultisampling;
        View stepFaceCulling;
        View stepFragmentShading;
        View stepDepthBufferTest;
        View stepBlending;
    }

    ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        DepthFunc = new GLConfig<Integer>(this, new Integer[] { GLES20.GL_NEVER, GLES20.GL_LESS, GLES20.GL_EQUAL,
                GLES20.GL_LEQUAL, GLES20.GL_GREATER, GLES20.GL_NOTEQUAL, GLES20.GL_GEQUAL, GLES20.GL_ALWAYS }, new String[] {
                "GL_NEVER", "GL_LESS", "GL_EQUAL", "GL_LEQUAL", "GL_GREATER", "GL_NOTEQUAL", "GL_GEQUAL", "GL_ALWAYS" },
                new int[] { R.string.gl_depth_never, R.string.gl_depth_less, R.string.gl_depth_equal, R.string.gl_depth_lequal,
                        R.string.gl_depth_greater, R.string.gl_depth_notequal, R.string.gl_depth_gequal,
                        R.string.gl_depth_always });

        BlendFunc = new GLConfig<Integer>(this, new Integer[] { GLES20.GL_ZERO, GLES20.GL_ONE, GLES20.GL_SRC_COLOR,
                GLES20.GL_ONE_MINUS_SRC_COLOR, GLES20.GL_DST_COLOR, GLES20.GL_ONE_MINUS_DST_COLOR, GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_DST_ALPHA, GLES20.GL_ONE_MINUS_DST_ALPHA, GLES20.GL_CONSTANT_COLOR,
                GLES20.GL_ONE_MINUS_CONSTANT_COLOR, GLES20.GL_CONSTANT_ALPHA, GLES20.GL_ONE_MINUS_CONSTANT_ALPHA },
                new String[] { "GL_ZERO", "GL_ONE", "GL_SRC_COLOR", "GL_ONE_MINUS_SRC_COLOR", "GL_DST_COLOR",
                        "GL_ONE_MINUS_DST_COLOR", "GL_SRC_ALPHA", "GL_ONE_MINUS_SRC_ALPHA", "GL_DST_ALPHA",
                        "GL_ONE_MINUS_DST_ALPHA", "GL_CONSTANT_COLOR", "GL_ONE_MINUS_CONSTANT_COLOR", "GL_CONSTANT_ALPHA",
                        "GL_ONE_MINUS_CONSTANT_ALPHA" }, new int[] { R.string.gl_blend_zero, R.string.gl_blend_one,
                        R.string.gl_blend_src_color, R.string.gl_blend_one_minus_src_color, R.string.gl_blend_dst_color,
                        R.string.gl_blend_one_minus_dst_color, R.string.gl_blend_src_alpha,
                        R.string.gl_blend_one_minus_src_alpha, R.string.gl_blend_dst_alpha,
                        R.string.gl_blend_one_minus_dst_alpha, R.string.gl_blend_constant_color,
                        R.string.gl_blend_one_minus_constant_color, R.string.gl_blend_constant_alpha,
                        R.string.gl_blend_one_minus_constant_alpha });

        // GL_MIN, GL_MAX are unsupported on this platform
        BlendEquation = new GLConfig<Integer>(this, new Integer[] { GLES20.GL_FUNC_ADD, GLES20.GL_FUNC_SUBTRACT,
                GLES20.GL_FUNC_REVERSE_SUBTRACT },
                new String[] { "GL_FUNC_ADD", "GL_FUNC_SUBTRACT", "GL_FUNC_REVERSE_SUBTRACT" }, new int[] {
                        R.string.gl_blend_add, R.string.gl_blend_sub, R.string.gl_blend_rev_sub });

        // TODO return from saved instance state or extras bundle
        mScrollView = (ScrollView) findViewById(R.id.setup_scrollview);

        // Find all views associated with individual pipeline steps
        steps.groupSceneDefinition = findViewById(R.id.group_scene_definition);
        steps.groupVertexProcessing = findViewById(R.id.group_vertex_processing);
        steps.groupPrimitiveProcessing = findViewById(R.id.group_primitive_processing);
        steps.groupRasterisation = findViewById(R.id.group_rasterisation);
        steps.groupFragmentProcessing = findViewById(R.id.group_fragment_processing);
        steps.groupPixelProcessing = findViewById(R.id.group_pixel_processing);

        steps.stepSceneComposition = findViewById(R.id.step_scene_composition);
        steps.stepCameraParameters = findViewById(R.id.step_camera_parameters);
        steps.stepLightPosition = findViewById(R.id.step_light_position);
        steps.stepLightingModel = findViewById(R.id.step_lighting_model);
        steps.stepAnimationDuration = findViewById(R.id.step_animation_duration);
        steps.stepVertexAssembly = findViewById(R.id.step_vertex_assembly);
        steps.stepVertexShading = findViewById(R.id.step_vertex_shading);
        steps.stepClipping = findViewById(R.id.step_clipping);
        steps.stepMultisampling = findViewById(R.id.step_multisampling);
        steps.stepFaceCulling = findViewById(R.id.step_face_culling);
        steps.stepFragmentShading = findViewById(R.id.step_fragment_shading);
        steps.stepDepthBufferTest = findViewById(R.id.step_depth_buffer_test);
        steps.stepBlending = findViewById(R.id.step_blending);

        // Put some interesting things in the scene
        Random r = new Random();
        for (int i = 0; i < 48; i++) {
            Element e = ShapeFactory.buildCuboid(new Float3(r.nextFloat() * 2 - 1, r.nextFloat() * 3 - 1.5f,
                    r.nextFloat() * 3 - 1.5f), r.nextFloat() / 5 + 0.1f, r.nextFloat() / 5 + 0.1f, r.nextFloat() / 5 + 0.1f,
                    Colour.RANDOM, Colour.RANDOM);
            mSceneElements.add(e);
        }

        initialiseViews();

        // Get shared preferences reference for first run detection
        prefs = getSharedPreferences("uk.co.ryft.pipeline", MODE_PRIVATE);
    }

    private boolean mHelpActivated = true;

    @Override
    protected void onResume() {
        super.onResume();

        // Show First Run help message if necessary
        if (prefs.getBoolean("firstrun", true)) {
            mHelpActivated = false;
            insertShowcaseView(new ActionItemTarget(this, R.id.action_setup_help), R.string.help_firstrun_title,
                    R.string.help_firstrun_desc, 1, null, new SimpleShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                            mHelpActivated = true;
                        }
                    });
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    // XXX To be called once, sets up listeners
    private void initialiseViews() {

        setText(steps.stepSceneComposition, android.R.id.title, R.string.button_scene_composition);
        setText(steps.stepCameraParameters, android.R.id.title, R.string.button_camera_parameters);
        setText(steps.stepLightPosition, android.R.id.title, R.string.button_light_position);
        setText(steps.stepLightingModel, android.R.id.title, R.string.button_lighting_model);
        setText(steps.stepAnimationDuration, android.R.id.title, R.string.button_animation_duration);
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

        steps.stepLightPosition.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate and display a float picker dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_light_position);

                LayoutInflater inflater = SetupActivity.this.getLayoutInflater();
                View dialogueView = inflater.inflate(R.layout.dialogue_point_edit, null);

                final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
                final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
                final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);

                builder.setView(dialogueView);
                builder.setPositiveButton(R.string.dialogue_button_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float x = (editX.getText().toString().length() == 0) ? 0 : Float.valueOf(editX.getText().toString());
                        float y = (editY.getText().toString().length() == 0) ? 0 : Float.valueOf(editY.getText().toString());
                        float z = (editZ.getText().toString().length() == 0) ? 0 : Float.valueOf(editZ.getText().toString());
                        mLightPosition = new Float3(x, y, z);
                        updateViews();
                    }
                });
                builder.setNegativeButton(R.string.dialogue_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                // Get the AlertDialog, initialise values and show it.
                AlertDialog dialogue = builder.create();

                editX.setText(String.valueOf(mLightPosition.getX()));
                editY.setText(String.valueOf(mLightPosition.getY()));
                editZ.setText(String.valueOf(mLightPosition.getZ()));
                dialogue.show();
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

        steps.stepAnimationDuration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate and display a float picker dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_animation_duration);

                final EditText durationTextView = new EditText(SetupActivity.this);
                durationTextView.setText(String.valueOf(mAnimationDuration));
                durationTextView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);

                builder.setView(durationTextView);
                builder.setPositiveButton(R.string.dialogue_button_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (durationTextView.getText().toString().length() > 0) {
                            mAnimationDuration = Integer.valueOf(durationTextView.getText().toString());
                            updateViews();
                        }
                    }
                });
                builder.setNegativeButton(R.string.dialogue_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                // Get the AlertDialog, initialise values and show it.
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

        steps.stepMultisampling.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate and display a configuration dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_multisampling);
                builder.setItems(new CharSequence[] { "2", "3", "4" }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMinSamples = which + 2;
                        updateViews();
                    }
                });
                AlertDialog dialogue = builder.create();
                dialogue.show();
            }
        });

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

        steps.stepDepthBufferTest.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Instantiate and display a configuration dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
                builder.setTitle(R.string.dialogue_title_depth_buffer_test);
                // builder.setMessage("Specifies the function used to compare each incoming pixel depth value with the depth value present in the depth buffer.");
                builder.setItems(DepthFunc.mNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDepthFunc = which;
                        updateViews();
                    }
                });
                AlertDialog dialogue = builder.create();
                dialogue.show();
            }

        });

        steps.stepBlending.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, SetupBlendingActivity.class);
                intent.putExtra("function_src", mBlendFuncSrc);
                intent.putExtra("function_dst", mBlendFuncDst);
                intent.putExtra("equation", mBlendEquation);
                startActivityForResult(intent, REQUEST_STEP_BLENDING);
            }

        });

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
                intent.putExtra("light_position", mLightPosition);
                intent.putExtra("animation_duration", mAnimationDuration);
                intent.putExtra("min_samples", mMinSamples);
                intent.putExtra("culling_clockwise", mCullingClockwise);
                intent.putExtra("depth_func", mDepthFunc);
                intent.putExtra("blend_func_src", mBlendFuncSrc);
                intent.putExtra("blend_func_dst", mBlendFuncDst);
                intent.putExtra("blend_equation", mBlendEquation);
                startActivity(intent);
            }
        });

        // Ensure all TextView marquees start scrolling
        mScrollView.setSelected(true);

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

        vertexAssemblySummary = String.valueOf(vertexCount);
        if (vertexCount == 1)
            vertexAssemblySummary += " vertex assembled from ";
        else
            vertexAssemblySummary += " vertices assembled from ";
        vertexAssemblySummary += primitiveCount + " primitive";
        if (primitiveCount != 1)
            vertexAssemblySummary += "s";

        // Generate light source position summary
        String lightPositionSummary = "Point source at " + mLightPosition.toString();

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

        // Generate animation duration summary
        String animationDurationSummary = "Transitions animate for " + mAnimationDuration + "ms";

        // Generate multisampling summary
        String multisamplingSummary = mMinSamples + " samples minimum in multisample buffers (" + mMinSamples + "x MSAA)";

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
        String depthBufferSummary = DepthFunc.mDescriptions[mDepthFunc];

        // Generate blending summary
        String blendingSummary = BlendEquation.mDescriptions[mBlendEquation];

        setText(steps.stepSceneComposition, android.R.id.summary, sceneCompositionSummary);
        setText(steps.stepCameraParameters, android.R.id.summary, cameraParametersSummary);
        setText(steps.stepLightPosition, android.R.id.summary, lightPositionSummary);
        setText(steps.stepLightingModel, android.R.id.summary, lightingModelSummary);
        setText(steps.stepAnimationDuration, android.R.id.summary, animationDurationSummary);
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
        return insertShowcaseView(new ViewTarget(target), title, description, scale, options, listener);
    }

    protected ShowcaseView insertShowcaseView(Target target, int title, int description, float scale, ConfigOptions options,
            OnShowcaseEventListener listener) {
        ShowcaseView sv = ShowcaseView.insertShowcaseView(target, this, title, description, options);
        sv.setOnShowcaseEventListener(listener);
        sv.setScaleMultiplier(scale);
        return sv;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup_help:
                if (!mHelpActivated)
                    break;
                
                final ScrollView scroll = mScrollView;

                final OnShowcaseEventListener tutorialSimulate = new SimpleShowcaseEventListener() {
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

                        // After the final tutrial screen, re-enable help button
                        mHelpActivated = true;
                    }
                };

                final OnShowcaseEventListener tutorialDisabledOptions = new SimpleShowcaseEventListener() {

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        scroll.smoothScrollTo(0, (int) steps.stepVertexAssembly.getY());
                        insertShowcaseView(steps.stepVertexAssembly.findViewById(android.R.id.title),
                                R.string.help_disabled_title, R.string.help_disabled_desc, 1.5f, null, tutorialSimulate);
                    }
                };

                final OnShowcaseEventListener tutorialLightingPreviews = new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        scroll.smoothScrollTo(0, (int) steps.stepLightingModel.getY());
                        insertShowcaseView(steps.stepLightingModel.findViewById(android.R.id.title),
                                R.string.help_lighting_preview_title, R.string.help_lighting_preview_desc, 1, null,
                                tutorialDisabledOptions);
                    }
                };

                final OnShowcaseEventListener tutorialColourCoding = new SimpleShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        scroll.smoothScrollTo(0, (int) steps.groupVertexProcessing.getY());
                        insertShowcaseView(steps.groupVertexProcessing.findViewById(R.id.swatch_vertex_processing),
                                R.string.help_colour_coding_title, R.string.help_colour_coding_desc, 0.5f, null,
                                tutorialLightingPreviews);
                    }
                };

                mHelpActivated = false;
                scroll.smoothScrollTo(0, 0);
                insertShowcaseView(steps.stepCameraParameters.findViewById(android.R.id.title),
                        R.string.help_scene_definition_title, R.string.help_scene_definition_desc, 1.5f, null,
                        tutorialColourCoding);

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

            case REQUEST_STEP_BLENDING:

                if (resultCode == RESULT_OK) {
                    mBlendFuncSrc = data.getIntExtra("function_src", mBlendFuncSrc);
                    mBlendFuncDst = data.getIntExtra("function_dst", mBlendFuncDst);
                    mBlendEquation = data.getIntExtra("equation", mBlendEquation);
                }
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
