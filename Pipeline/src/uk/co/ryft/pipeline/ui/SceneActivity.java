package uk.co.ryft.pipeline.ui;

import java.util.ArrayList;
import java.util.Collection;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.Composite;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;

public class SceneActivity extends ListActivity {

    // Request values
    protected static final int ADD_ELEMENT_REQUEST = 2;
    protected static final int EDIT_ELEMENT_REQUEST = 3;

    protected ElementAdapter mAdapter;

    // XXX Need to store this reference to be able to update/delete.
    // If we pass the worked-on element back and forth, it gets serialised and
    // we lose the reference.
    protected Primitive mThisElement;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        setupActionBar();

        // Get elements from returning activity intent or saved state, if
        // possible.
        Bundle extras = getIntent().getExtras();

        ArrayList<Element> elements;
        if (savedInstanceState != null && savedInstanceState.containsKey("elements")) {
            elements = (ArrayList<Element>) savedInstanceState.getSerializable("elements");

        } else if (extras != null && extras.containsKey("elements")) {
            elements = (ArrayList<Element>) extras.getSerializable("elements");

        } else {
            elements = new ArrayList<Element>();
        }

        mAdapter = new ElementAdapter(this, elements);
        setListAdapter(mAdapter);
        
        ListView listView = getListView();
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
                listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            mAdapter.remove((Element) mAdapter.getItem(position));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        result.putExtra("elements", mAdapter.getAllElements());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
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
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                saveAndQuit();

            case R.id.action_elements_new:
                addElement();
                break;

            case R.id.action_elements_discard:
                mAdapter.clear();
                Toast.makeText(this, R.string.message_scene_clear, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addElement() {
        Intent intent = new Intent(this, ElementActivity.class);
        intent.putExtra("edit_mode", false);
        startActivityForResult(intent, ADD_ELEMENT_REQUEST);
    }

    protected void editPrimitive(Primitive e) {
        Intent intent = new Intent(this, ElementActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("element", e);
        mThisElement = e;
        startActivityForResult(intent, EDIT_ELEMENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String message = "";

        // Parameter key:
        //
        // For adding a new element (ADD_ELEMENT_REQUEST)
        // OK result -> new element is 'element' extra
        // Otherwise -> take no action
        //
        // For editing an element (EDIT_ELEMENT_REQUEST)
        // OK result -> new element is 'element' extra
        // >> If 'deleted' is set true, remove old element
        // >> Otherwise replace old element with new
        // Otherwise -> take no action

        // If the request was ADD_ELEMENT_REQUEST
        if (requestCode == ADD_ELEMENT_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                if (!data.getBooleanExtra("delete", false)) {
                    mAdapter.add((Primitive) data.getSerializableExtra("element"));
                    message = getString(R.string.message_element_added);
                    
                } else
                    message = getString(R.string.message_element_discarded);
            }

        // If the request was EDIT_ELEMENT_REQUEST
        } else if (requestCode == EDIT_ELEMENT_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                mAdapter.remove(mThisElement);
                mThisElement = null;
                
                if (!data.getBooleanExtra("delete", false)) {
                    Primitive newElement = (Primitive) data.getSerializableExtra("element");
                    mAdapter.add(newElement);
                    message = getString(R.string.message_element_updated);
                }
                // Else original element has been deleted as required
                // A suitable message has already been displayed to the user
                
            } else {
                mThisElement = null;
                System.out.println(requestCode);
                message = getString(R.string.message_element_deleted);
            }

        } else {
            // Add any new result codes here.
            throw new UnsupportedOperationException();
        }

        if (message != "")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putSerializable("elements", mAdapter.getAllElements());
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

    class ElementAdapter extends BaseAdapter {

        final Context mContext;
        final ArrayList<Element> mElems;
        final LayoutInflater mInflater;

        public ElementAdapter(Context context, Collection<Element> elements) {
            super();

            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mElems = new ArrayList<Element>(elements);
        }

        public boolean add(Primitive element) {
            boolean ret = mElems.add(element);
            notifyDataSetChanged();
            return ret;
        }

        public boolean addAll(Collection<Element> elements) {
            boolean ret = mElems.addAll(elements);
            notifyDataSetChanged();
            return ret;
        }

        public boolean remove(Element element) {
            boolean ret = mElems.remove(element);
            notifyDataSetChanged();
            return ret;
        }

        public boolean removeAll(Collection<Element> elements) {
            boolean ret = mElems.removeAll(elements);
            notifyDataSetChanged();
            return ret;
        }

        @SuppressWarnings("unchecked")
        public ArrayList<Element> getAllElements() {
            return (ArrayList<Element>) mElems.clone();
        }

        public void clear() {
            mElems.clear();
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // Recycle view if possible
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_element, null);
            }

            ImageView elemIcon = (ImageView) convertView.findViewById(R.id.element_icon);
            TextView typeTextView = (TextView) convertView.findViewById(R.id.element_type);
            ImageButton editButton = (ImageButton) convertView
                    .findViewById(R.id.button_element_edit);
            TextView summaryTextView = (TextView) convertView.findViewById(R.id.element_summary);

            Element elem = mElems.get(position);

            if (elem != null) {
                
                final boolean isPrimitive = elem.isPrimitive();
                
                elemIcon.setImageResource(elem.getIconRef());
                typeTextView.setText(elem.getTitle());
                summaryTextView.setText(elem.getSummary());
                
                typeTextView.setClickable(false);
                
                if (isPrimitive)
                    editButton.setImageResource(R.drawable.ic_action_edit);
                else
                    editButton.setImageResource(R.drawable.ic_action_expand);

                editButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Element elem = (Element) getItem(position);
                        if (isPrimitive)
                            // XXX safe cast due to previous check.
                            editPrimitive((Primitive) elem);
                        else {
                            Collection<Element> components = ((Composite) elem).getComponents();
                            remove(elem);
                            addAll(components);
                            
                            String message = "Expanded " + components.size() + " component";
                            if (components.size() != 1)
                                message += "s";
                            Toast.makeText(SceneActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return mElems.size();
        }

        @Override
        public Object getItem(int position) {
            return mElems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

}
