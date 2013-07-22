
package uk.co.ryft.pipeline.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Element;

public class ElementActivity extends Activity {

    boolean edit_mode;
    Element oldElem;
    Element newElem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle fromScene = this.getIntent().getExtras();
        edit_mode = fromScene.getBoolean("edit_mode");

        if (edit_mode) {
            oldElem = (Element) fromScene.getSerializable("element");
            newElem = (Element) oldElem.clone();

            setContentView(R.layout.activity_element_edit);

            final Button button_delete = (Button) findViewById(R.id.button_element_delete);
            button_delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent result = new Intent();
                    result.putExtra("deleted", true);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
        }

        else {
            setContentView(R.layout.activity_element_add);

        }

        final Button button_save = (Button) findViewById(R.id.button_element_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent result = new Intent();
                if (edit_mode)
                    result.putExtra("element_old", oldElem);
                result.putExtra("element_new", newElem); // TODO
                setResult(RESULT_OK, result);
                finish();
            }
        });

        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.element, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be
        // "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }

}
