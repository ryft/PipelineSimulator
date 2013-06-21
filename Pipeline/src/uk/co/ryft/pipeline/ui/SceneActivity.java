
package uk.co.ryft.pipeline.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.menu.ActionBarActivity;
import uk.co.ryft.pipeline.model.Element;

import java.util.ArrayList;

public class SceneActivity extends ActionBarActivity {

    protected static final int ADD_ELEMENT_REQUEST = 2;
    protected static final int EDIT_ELEMENT_REQUEST = 3;

    protected ElementListView mListView;
    protected ArrayList<Element> mElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);

        mListView = (ElementListView) findViewById(R.id.element_list);
        @SuppressWarnings("unchecked")
        ArrayList<Element> oldElems =
                (ArrayList<Element>) getIntent().getExtras().getSerializable("elements");
        mElements = oldElems;

        final ElementListAdapter adapter =
                new ElementListAdapter(this, R.id.element_type, mElements);
        mListView.setAdapter(adapter);
    }

    class ElementListAdapter extends ArrayAdapter<Element> {

        final Context mContext;
        final ArrayList<Element> mElems;
        final LayoutInflater mInflater;

        public ElementListAdapter(Context context, int textViewResourceId,
                ArrayList<Element> elements) {
            super(context, textViewResourceId);
            mContext = context;
            mElems = elements;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (Element e : elements)
                add(e);

            final Button button = (Button) findViewById(R.id.button_save);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent result = new Intent();
                    result.putExtra("elements", mElems);
                    setResult(Activity.RESULT_OK, result);
                    finish();
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Recycle view if possible
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_element, null);
            }

            ImageView elemImageView = (ImageView) convertView.findViewById(R.id.element_icon);
            TextView typeTextView = (TextView) convertView.findViewById(R.id.element_type);
            ImageView forwardImageView = (ImageView) convertView.findViewById(R.id.element_edit);
            TextView summaryTextView = (TextView) convertView.findViewById(R.id.element_summary);

            elemImageView.setImageResource(R.drawable.no);
            typeTextView.setText(mElems.get(position).getType().toString());
            forwardImageView.setImageResource(R.drawable.forward);
            summaryTextView.setText("Summary text");

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.scene, menu);

        // Calling super after populating the menu is necessary here to ensure
        // that the action bar helpers have a chance to handle this event.
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
        startActivityForResult(intent, ADD_ELEMENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // If the request went well (OK) and the request was ADD_ELEMENT
        if (resultCode == Activity.RESULT_OK && requestCode == ADD_ELEMENT_REQUEST) {

            Toast.makeText(this, "Created element", Toast.LENGTH_SHORT).show();

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

class ElementListView extends ListView {

    public ElementListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
