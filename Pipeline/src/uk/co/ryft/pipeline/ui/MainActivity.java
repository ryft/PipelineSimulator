package uk.co.ryft.pipeline.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {

    protected PipelineSurface mPipelineView;
    protected ArrayList<Element> mElements;

    protected static final int EDIT_SCENE_REQUEST = 1;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupActionBar();

        mPipelineView = (PipelineSurface) findViewById(R.id.pipeline_surface);

        // Get elements from returning activity intent or saved state, if
        // possible.
        Bundle extras = getIntent().getExtras();

        if (savedInstanceState != null && savedInstanceState.containsKey("elements")) {
            mElements = (ArrayList<Element>) savedInstanceState.getSerializable("elements");

        } else if (extras != null && extras.containsKey("elements")) {
            mElements = (ArrayList<Element>) extras.getSerializable("elements");

        } else {
            mElements = new ArrayList<Element>();
        }

        List<Drawable> scene = new LinkedList<Drawable>();
        for (Element e : mElements) {
            Drawable d = e.getDrawable();
            scene.add(d);
        }
        
        // XXX Restore scene state
        if (savedInstanceState != null) {
            PipelineRenderer renderer = mPipelineView.getRenderer();
            
            mPipelineView.setEditMode(savedInstanceState.getBoolean("edit_mode"));
            renderer.setRotation(savedInstanceState.getFloat("angle"));
            if (savedInstanceState.getSerializable("actual_camera") != null)
                renderer.setActualCamera((Camera) savedInstanceState.getSerializable("actual_camera"));
            if (savedInstanceState.getSerializable("virtual_camera") != null)
                renderer.setVirtualCamera((Camera) savedInstanceState.getSerializable("virtual_camera"));
        }
        
        updateScene();
        
    }
    
    @Override
    public void onBackPressed() {
        if (mPipelineView.isEditMode())
            mPipelineView.toggleEditMode();
        else
            super.onBackPressed();
    }
    
//    private void printVector(float[] v) {
//        System.out.print("[ ");
//        for (int i = 0; i < v.length; i++)
//            System.out.print(v[i]+" ");
//        System.out.println("]");
//    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    protected void updateScene() {
        mPipelineView.updateScene(mElements);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;

            case R.id.action_scene:
                Intent intent = new Intent(this, SceneActivity.class);
                intent.putExtra("elements", mElements);
                startActivityForResult(intent, EDIT_SCENE_REQUEST);
                break;
                
            case R.id.action_draw_axes:

                mElements.add(ShapeFactory.buildCuboid(new Float3(0, 0, 0), 0.5f, 0.5f, 0.5f, Colour.GREEN, Colour.RED));
                
                updateScene();
                break;
                
            case R.id.action_change_perspective:
                mPipelineView.toggle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == EDIT_SCENE_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                @SuppressWarnings("unchecked")
                ArrayList<Element> newElems = (ArrayList<Element>) data.getExtras()
                        .getSerializable("elements");
                mElements = newElems;
                updateScene();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        PipelineRenderer renderer = mPipelineView.getRenderer();
        
        savedInstanceState.putSerializable("elements", mElements);
        savedInstanceState.putBoolean("edit_mode", mPipelineView.isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
        savedInstanceState.putSerializable("actual_camera", renderer.getActualCamera());
        savedInstanceState.putSerializable("virtual_camera", renderer.getVirtualCamera());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPipelineView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPipelineView.onResume();
    }

    public PipelineSurface getPipelineView() {
        return mPipelineView;
    }

    public void setPipelineView(PipelineSurface mPipelineView) {
        this.mPipelineView = mPipelineView;
    }

}
