
package uk.co.ryft.pipeline.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.action.ActionBarActivity;
import uk.co.ryft.pipeline.gl.PipelineSurface;

public class MainActivity extends ActionBarActivity {
    
    private PipelineSurface mPipelineView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mPipelineView = (PipelineSurface) findViewById(R.id.pipelineSurface);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        // Calling super after populating the menu is necessary here to ensure that the
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
                startActivity(intent);
                break;

            case R.id.menu_search:
                Toast.makeText(this, "Zoomed out to level "+mPipelineView.zoomOut(), Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_share:
                break;
        }
        return super.onOptionsItemSelected(item);
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
