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
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.model.shapes.Primitive.Type;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.OpacityBar;

public class PrimitiveActivity extends ListActivity {

    protected ArrayAdapter<Float3> mAdapter;
    protected Primitive mElement;
    protected TypeSpinner mTypeSpinner;
    
    protected boolean mEditMode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_primitive);

        // Parse data from parent activity
        Bundle fromScene = this.getIntent().getExtras();
        mEditMode = fromScene.getBoolean("edit_mode", false);
        
        if (mEditMode) {
            mElement = (Primitive) fromScene.getSerializable("element");
            setTitle(R.string.title_activity_primitive_edit);
        } else {
            mElement = new Primitive(Type.GL_POINTS);
            setTitle(R.string.title_activity_primitive_add);
        }
        
        // Set up save / delete button listeners
        Button saveButton = (Button) findViewById(R.id.button_element_save);
        Button deleteButton = (Button) findViewById(R.id.button_element_discard);
        
        if (mEditMode)
            deleteButton.setText(R.string.action_element_delete);
        else
            deleteButton.setText(R.string.action_element_discard);
        
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndQuit(false);
            }
        });
        
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndQuit(true);
            }
        });

        // Set current element properties as default selections
        mTypeSpinner = (TypeSpinner) findViewById(R.id.element_type_spinner);
        TypeSpinnerAdapter typeAdapter = new TypeSpinnerAdapter(this,
                android.R.layout.simple_list_item_1, Primitive.Type.values());
        mTypeSpinner.setAdapter(typeAdapter);
        mTypeSpinner.setSelection(mElement.getType());

        mAdapter = new ArrayAdapter<Float3>(this, R.layout.listitem_point,
                R.id.text_point, mElement.getVertices());
        setListAdapter(mAdapter);

        // The below is copied verbatim from https://github.com/romannurik/Android-SwipeToDismiss
        ListView listView = getListView();
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
                listView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            mAdapter.remove((Float3) mAdapter.getItem(position));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());

        // Set up list item click event handler.
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // Instantiate and display a float picker dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(PrimitiveActivity.this);
                builder.setTitle(R.string.dialogue_title_point);

                LayoutInflater inflater = PrimitiveActivity.this.getLayoutInflater();
                View dialogueView = inflater.inflate(R.layout.dialogue_point_edit, null);

                final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
                final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
                final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);
                final Float3 thisPoint = mAdapter.getItem(position);

                builder.setView(dialogueView);
                builder.setPositiveButton(R.string.dialogue_button_save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                float x = Float.valueOf(editX.getText().toString());
                                float y = Float.valueOf(editY.getText().toString());
                                float z = Float.valueOf(editZ.getText().toString());
                                thisPoint.setCoordinates(x, y, z);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton(R.string.dialogue_button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                // Get the AlertDialog, initialise values and show it.
                AlertDialog dialogue = builder.create();

                editX.setText(String.valueOf(thisPoint.getX()));
                editY.setText(String.valueOf(thisPoint.getY()));
                editZ.setText(String.valueOf(thisPoint.getZ()));
                dialogue.show();
            }
        });
        
        final ImageButton buttonColour = (ImageButton) findViewById(R.id.button_element_colour);
        final View swatch = (View) findViewById(R.id.element_colour_swatch);
        swatch.setBackgroundColor(mElement.getColourArgb());
        buttonColour.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Instantiate and display a float picker dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(PrimitiveActivity.this);
                builder.setTitle(R.string.dialogue_title_colour);

                LayoutInflater inflater = PrimitiveActivity.this.getLayoutInflater();
                View dialogueView = inflater.inflate(R.layout.dialogue_colour_select, null);
                
                builder.setView(dialogueView);
                
                final ColorPicker picker = (ColorPicker) dialogueView.findViewById(R.id.picker);
                OpacityBar opacityBar = (OpacityBar) dialogueView.findViewById(R.id.opacitybar);
                picker.addOpacityBar(opacityBar);
                picker.setOldCenterColor(mElement.getColourArgb());

                builder.setPositiveButton(R.string.dialogue_button_save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int color = picker.getColor();
                                // TODO this is terrible and unsafe.
                                mElement.setColour(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
                                swatch.setBackgroundColor(color);
                            }
                        });
                builder.setNegativeButton(R.string.dialogue_button_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                // Get the AlertDialog, initialise values and show it.
                AlertDialog dialogue = builder.create();
                dialogue.show();
            }
            
        });
        
        setupActionBar();
    }

    long mBackPressed = 0;
    
    @Override
    public void onBackPressed() {
        long time = SystemClock.uptimeMillis();
        long elapsed = time - mBackPressed;
        
        if (elapsed < 2000)
            discardAndQuit();
        
        else {
            if (mEditMode)
                Toast.makeText(this, R.string.warning_element_discard_changes, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.warning_element_discard, Toast.LENGTH_SHORT).show();
            mBackPressed = time;
        }
    }

    protected void saveAndQuit(boolean delete) {
        Intent result = new Intent();
        result.putExtra("delete", delete);

        if (!delete) {
            mElement.setType((Type) mTypeSpinner.getSelectedItem());
            LinkedList<Float3> points = new LinkedList<Float3>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                points.add((Float3) mAdapter.getItem(i));
            }
            mElement.setVertices(points);
            result.putExtra("element", mElement);
        }
        
        setResult(RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
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
                saveAndQuit(false);

            case R.id.action_points_new:
                mAdapter.add(new Float3(0f, 0f, 0f));
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.action_points_discard:
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: Consider using a BaseAdapter -- see
    // http://www.piwai.info/android-adapter-good-practices/
    // TODO: Consider using a ViewHolder -- see
    // http://www.google.com/events/io/2010/sessions/world-of-listview-android.html
    class PointAdapter extends ArrayAdapter<Float3> {

        final Context mContext;
        final ArrayList<Float3> mPoints;
        final LayoutInflater mInflater;

        public PointAdapter(Context context, Collection<Float3> points) {
            super(context, 0);

            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPoints = new ArrayList<Float3>(points);

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // Recycle view if possible
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.listitem_point, null);
            // XXX All events are handled from the listview (parent) level.
            return convertView;
        }

        protected void updatePoint(int position, float x, float y, float z) {
            Float3 point = (Float3) getItem(position);
            point.setX(x);
            point.setY(y);
            point.setZ(z);
        }

    }
}
