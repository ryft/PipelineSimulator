
package uk.co.ryft.pipeline.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.PipelineSurface;
import uk.co.ryft.pipeline.menu.ActionBarActivity;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Element.Type;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    protected PipelineSurface mPipelineView;
    protected ArrayList<Element> mElements;

    protected static final int EDIT_SCENE_REQUEST = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPipelineView = (PipelineSurface) findViewById(R.id.pipeline_surface);
        mElements = new ArrayList<Element>();

        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.TRIANGLE_FAN, null));
        mElements.add(new Element(Type.LINES, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.TRIANGLE_FAN, null));
        mElements.add(new Element(Type.LINES, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.POINTS, null));
        mElements.add(new Element(Type.TRIANGLE_FAN, null));
        mElements.add(new Element(Type.LINES, null));
        mElements.add(new Element(Type.POINTS, null));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;

            case R.id.menu_edit_scene:
                Intent intent = new Intent(this, SceneActivity.class);
                intent.putExtra("elements", mElements);
                startActivityForResult(intent, EDIT_SCENE_REQUEST);
                break;

            case R.id.menu_search:
                Toast.makeText(this, "Zoomed out to level " + mPipelineView.zoomOut(),
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == EDIT_SCENE_REQUEST) {

            if (resultCode == Activity.RESULT_OK && requestCode == EDIT_SCENE_REQUEST) {
                @SuppressWarnings("unchecked")
                ArrayList<Element> newElems = (ArrayList<Element>) data.getExtras()
                        .getSerializable("elements");
                mElements = newElems;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPipelineView.onPause();
    }

    @Override
    protected void onResume() {
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
