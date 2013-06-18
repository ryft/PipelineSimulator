
package uk.co.ryft.pipeline.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.action.ActionBarActivity;

public class SceneActivity extends ActionBarActivity {

    protected static final int ADD_ELEMENT = 2;
    protected static final int EDIT_ELEMENT = 3;
    
    protected ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        mListView = (ListView) findViewById(R.id.elementListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.scene, menu);

        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                createElement();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected void createElement() {
        Intent intent = new Intent(this, ElementActivity.class);
        startActivityForResult(intent, ADD_ELEMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        // If the request went well (OK) and the request was PICK_CONTACT_REQUEST
        if (resultCode == Activity.RESULT_OK && requestCode == ADD_ELEMENT) {

            System.out.println("CREATED ELEMENT");
            Toast.makeText(this, "Created element", Toast.LENGTH_SHORT).show();
            
            // Perform a query to the contact's content provider for the contact's name
//            Cursor cursor = getContentResolver().query(data.getData(),
//            new String[] {Contacts.DISPLAY_NAME}, null, null, null);
//            if (cursor.moveToFirst()) { // True if the cursor is not empty
//                int columnIndex = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
//                String name = cursor.getString(columnIndex);
//                // Do something with the selected contact's name...
//            }
        }
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
        // Another activity is taking focus (this activity is about to be "paused").
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
