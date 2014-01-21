package uk.co.ryft.pipeline;

import java.util.ArrayList;

import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.ui.setup.SetupCullingActivity;
import uk.co.ryft.pipeline.ui.setup.SetupSceneActivity;
import uk.co.ryft.pipeline.ui.simulator.SimulatorActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SetupActivity extends Activity {
    
    // Activity request codes
    protected static final int REQUEST_STEP_SCENE = 2;
    protected static final int REQUEST_STEP_CULLING = 3;

    // Global state set by step configuration
    // Scene composition
    protected ArrayList<Element> mSceneElements = new ArrayList<Element>();
    // Face culling
    protected boolean mCullingEnabled = true;
    protected boolean mCullingClockwise = false;
    
    ViewHolder steps = new ViewHolder();

    static class ViewHolder {
        View sceneComposition;
        View vertexShading;
        View geometryShading;
        View clipping;
        View culling;
        View fragmentShading;
        View depthBufferTest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // TODO return from saved instance state or extras bundle

        // Find all views associated with individual pipeline steps
        steps.sceneComposition = findViewById(R.id.step_scene_composition);
        steps.vertexShading = findViewById(R.id.step_vertex_shading);
        steps.geometryShading = findViewById(R.id.step_geometry_shading);
        steps.clipping = findViewById(R.id.step_clipping);
        steps.culling = findViewById(R.id.step_culling);
        steps.fragmentShading = findViewById(R.id.step_fragment_shading);
        steps.depthBufferTest = findViewById(R.id.step_depth_buffer_test);

        initialiseViews();
    }

    private void initialiseViews() {

        setText(steps.sceneComposition, android.R.id.title, R.string.button_scene_composition);
        setText(steps.vertexShading, android.R.id.title, R.string.button_vertex_shading);
        setText(steps.geometryShading, android.R.id.title, R.string.button_geometry_shading);
        setText(steps.clipping, android.R.id.title, R.string.button_clipping);
        setText(steps.culling, android.R.id.title, R.string.button_culling);
        setText(steps.fragmentShading, android.R.id.title, R.string.button_fragment_shading);
        setText(steps.depthBufferTest, android.R.id.title, R.string.button_depth_buffer_test);

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

        steps.culling.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, SetupCullingActivity.class);
                intent.putExtra("enabled", mCullingEnabled);
                intent.putExtra("clockwise", mCullingClockwise);
                startActivityForResult(intent, REQUEST_STEP_CULLING);
            }
        });
        
        Button buttonSimulate = (Button) findViewById(R.id.button_simulate);
        buttonSimulate.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupActivity.this, SimulatorActivity.class);
                intent.putExtra("elements", mSceneElements);
                startActivity(intent);
            }
        });

        updateViews();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        switch (requestCode) {
            case REQUEST_STEP_SCENE:
                
                if (resultCode == RESULT_OK)
                    mSceneElements = ((ArrayList<Element>) data.getSerializableExtra("elements"));
                break;
                
            case REQUEST_STEP_CULLING:
                
                if (resultCode == RESULT_OK) {
                    mCullingEnabled = data.getBooleanExtra("enabled", true);
                    mCullingClockwise = data.getBooleanExtra("clockwise", false);
                }
                break;
                
            default:
                // Add any new result codes here.
                throw new UnsupportedOperationException();
        }
        
        updateViews();
        
    }

    private void updateViews() {

        // Generate scene composition summary
        String sceneCompositionSummary = mSceneElements.size() + " element";
        if (mSceneElements.size() != 1)
            sceneCompositionSummary += "s";
        
        int primitiveCount = 0;
        for (Element e : mSceneElements)
            primitiveCount += e.getPrimitiveCount();
        
        int vertexCount = 0;
        for (Element e : mSceneElements)
            vertexCount += e.getVertexCount();
        
        sceneCompositionSummary += " (" + primitiveCount + " primitive";
        if (primitiveCount != 1)
            sceneCompositionSummary += "s";
        
        sceneCompositionSummary += ", " + vertexCount;
        if (vertexCount == 1)
            sceneCompositionSummary += " vertex)";
        else
            sceneCompositionSummary += " vertices)";
        
        // Generate face culling summary
        String cullingSummary = "Culling ";
        if (mCullingEnabled) {
            cullingSummary += "enabled (";
            if (!mCullingClockwise)
                cullingSummary += "counter-";
            cullingSummary += "clockwise winding)";
        } else
            cullingSummary += "disabled";
        
        // TODO Update these properly

        setText(steps.sceneComposition, android.R.id.summary, sceneCompositionSummary);
        setText(steps.vertexShading, android.R.id.summary, "Empty vertex shader");
        setText(steps.geometryShading, android.R.id.summary, "Empty geometry shader");
        setText(steps.clipping, android.R.id.summary, "Default clipping");
        setText(steps.culling, android.R.id.summary, cullingSummary);
        setText(steps.fragmentShading, android.R.id.summary, "Empty fragment shader");
        setText(steps.depthBufferTest, android.R.id.summary, "Default depth buffer test");

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
