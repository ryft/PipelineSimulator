/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//  Adapted from https://github.com/romannurik/Android-SwipeToDismiss

package uk.co.ryft.pipeline.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.FloatPoint;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Element.Type;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;

public class DismissableActivity extends ListActivity {

    protected PointAdapter mAdapter;
    protected Element mElement;
    protected TypeSpinner mTypeSpinner;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dismissable_list_view);

        Bundle fromScene = this.getIntent().getExtras();
        if (fromScene.getBoolean("edit_mode", false))
            mElement = (Element) fromScene.getSerializable("element");
        else
            mElement = new Element(Type.GL_POINTS);

        mAdapter = new PointAdapter(this, mElement.getVertices());
        setListAdapter(mAdapter);

        ListView listView = getListView();
        // Create a ListView-specific touch listener. ListViews are given
        // special treatment because
        // by default they handle touches for their list items... i.e. they're
        // in charge of drawing
        // the pressed state (the list selector), handling list item clicks,
        // etc.
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
                listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            mAdapter.remove((FloatPoint) mAdapter.getItem(position));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during
        // ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

        mTypeSpinner = (TypeSpinner) findViewById(R.id.element_type_spinner);
        TypeSpinnerAdapter typeAdapter = new TypeSpinnerAdapter(this,
                android.R.layout.simple_list_item_1, Element.Type.values());
        mTypeSpinner.setAdapter(typeAdapter);

        setupActionBar();
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();

        mElement.setType((Type) mTypeSpinner.getSelectedItem());
        LinkedList<FloatPoint> points = new LinkedList<FloatPoint>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            points.add((FloatPoint) mAdapter.getItem(i));
        }
        mElement.setVertices(points);
        result.putExtra("element", mElement);

        setResult(RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void setupActionBar() {
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
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
                // NavUtils.navigateUpFromSameTask(this);
                saveAndQuit();

            case R.id.action_point_new:
                mAdapter.add(new FloatPoint(0f, 0f, 0f));
                break;

            case R.id.action_element_discard:
                discardAndQuit();
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: Consider using a BaseAdapter -- see
    // http://www.piwai.info/android-adapter-good-practices/
    // TODO: Consider using a ViewHolder -- see
    // http://www.google.com/events/io/2010/sessions/world-of-listview-android.html
    class PointAdapter extends BaseAdapter {

        final Context mContext;
        final ArrayList<FloatPoint> mPoints;
        final LayoutInflater mInflater;

        public PointAdapter(Context context, Collection<FloatPoint> points) {
            super();

            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPoints = new ArrayList<FloatPoint>(points);

        }

        public boolean add(FloatPoint point) {
            boolean ret = mPoints.add(point);
            notifyDataSetChanged();
            return ret;
        }

        public boolean remove(FloatPoint point) {
            boolean ret = mPoints.remove(point);
            notifyDataSetChanged();
            return ret;
        }

        @Override
        public int getCount() {
            return mPoints.size();
        }

        @Override
        public Object getItem(int position) {
            return mPoints.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Recycle view if possible
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.listitem_point, null);

            final EditText pointX = (EditText) convertView.findViewById(R.id.edit_point_x);
            final EditText pointY = (EditText) convertView.findViewById(R.id.edit_point_y);
            final EditText pointZ = (EditText) convertView.findViewById(R.id.edit_point_z);

            final int pos = position;

            TextWatcher listener = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        float x = Float.parseFloat(pointX.getText().toString());
                        float y = Float.parseFloat(pointY.getText().toString());
                        float z = Float.parseFloat(pointZ.getText().toString());
                        updatePoint(pos, x, y, z);
                    } catch (NumberFormatException e) {
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            };

            FloatPoint point = (FloatPoint) getItem(position);
            pointX.setText(String.valueOf(point.getX()));
            pointY.setText(String.valueOf(point.getY()));
            pointZ.setText(String.valueOf(point.getZ()));

            pointX.addTextChangedListener(listener);
            pointY.addTextChangedListener(listener);
            pointZ.addTextChangedListener(listener);

            return convertView;
        }

        protected void updatePoint(int position, float x, float y, float z) {
            FloatPoint point = (FloatPoint) getItem(position);
            point.setX(x);
            point.setY(y);
            point.setZ(z);
        }

    }
}
