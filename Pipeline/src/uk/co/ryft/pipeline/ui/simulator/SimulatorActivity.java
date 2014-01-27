package uk.co.ryft.pipeline.ui.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Drawable;
import uk.co.ryft.pipeline.gl.PipelineRenderer;
import uk.co.ryft.pipeline.model.Element;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SimulatorActivity extends Activity {

    private static final String TAG = "SimulatorActivity";

    protected PipelineSurface mPipelineView;
    protected ArrayList<Element> mElements;
    protected Bundle mPipelineParams;

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data from activity intent
        mPipelineParams = getIntent().getExtras();
        mPipelineView = new PipelineSurface(this, mPipelineParams);
        setContentView(mPipelineView);
        mPipelineView.setPadding(2, 2, 2, 2);

        mElements = (ArrayList<Element>) mPipelineParams.getSerializable("elements");
        List<Drawable> scene = new LinkedList<Drawable>();
        for (Element e : mElements) {
            Drawable d = e.getDrawable();
            scene.add(d);
        }

    }

    @Override
    public void onBackPressed() {
        if (mPipelineView.isEditMode())
            mPipelineView.toggleEditMode();
        else
            super.onBackPressed();
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
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        PipelineRenderer renderer = mPipelineView.getRenderer();

        savedInstanceState.putBoolean("edit_mode", mPipelineView.isEditMode());
        savedInstanceState.putFloat("angle", renderer.getRotation());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        PipelineRenderer renderer = mPipelineView.getRenderer();

        mPipelineView.setEditMode(savedInstanceState.getBoolean("edit_mode", false));
        renderer.setRotation(savedInstanceState.getFloat("angle", 0));
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

}
