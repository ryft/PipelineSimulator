package uk.co.ryft.pipeline.ui.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Camera;
import uk.co.ryft.pipeline.model.Element;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SimulatorActivity extends Activity {

    protected PipelineSurface mPipelineView;
    protected ArrayList<Element> mElements;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data from returning activity intent or saved state, if possible
        Bundle data = getIntent().getExtras();
        
        mPipelineView = new PipelineSurface(this, data);
        setContentView(mPipelineView);
        mPipelineView.setPadding(2, 2, 2, 2);

        if (savedInstanceState != null)
            data = savedInstanceState;
        
        mElements = (ArrayList<Element>) data.getSerializable("elements");

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

            case R.id.action_change_perspective:
                mPipelineView.toggle();
                break;
        }
        return super.onOptionsItemSelected(item);
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
