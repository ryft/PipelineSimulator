package uk.co.ryft.pipeline.ui.setup.builder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Colour;
import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.Float3;
import uk.co.ryft.pipeline.model.element.Primitive;
import uk.co.ryft.pipeline.model.element.Primitive.Type;
import uk.co.ryft.pipeline.ui.component.EditColourHandler;
import uk.co.ryft.pipeline.ui.component.EditColourHandler.OnColourChangedListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;

public class BuildPrimitiveActivity extends ListActivity {

    // Build an adapter of Float3s because they are mutable
    protected ArrayAdapter<Float3> mAdapter;
    // protected Primitive mElement;
    protected Type mElementType;
    protected List<Float3> mElementVertices;
    protected Colour mElementColour;
    protected TypeSpinner mTypeSpinner;

    protected boolean mEditMode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_primitive);

        // Parse data from parent activity
        Bundle fromScene = this.getIntent().getExtras();
        mEditMode = fromScene.getBoolean("edit_mode", false);

        Primitive primitive;
        if (mEditMode) {
            primitive = (Primitive) fromScene.getSerializable("element");
            setTitle(R.string.title_activity_primitive_edit);
        } else {
            primitive = new Primitive(Type.GL_POINTS);
            setTitle(R.string.title_activity_primitive_add);
        }
        mElementType = primitive.getType();
        mElementVertices = primitive.getVertices();
        mElementColour = primitive.getColour();

        // Set up save / delete button listeners
        Button saveButton = (Button) findViewById(R.id.button_row_positive);
        Button deleteButton = (Button) findViewById(R.id.button_row_negative);

        if (mEditMode)
            deleteButton.setText(R.string.action_button_delete);
        else
            deleteButton.setText(R.string.action_button_discard);

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
        TypeSpinnerAdapter typeAdapter = new TypeSpinnerAdapter(this, android.R.layout.simple_list_item_1,
                Primitive.Type.values());
        mTypeSpinner.setAdapter(typeAdapter);
        mTypeSpinner.setSelection(mElementType);

        mAdapter = new ArrayAdapter<Float3>(this, R.layout.listitem_point, R.id.text_point, mElementVertices);
        setListAdapter(mAdapter);

        ListView listView = getListView();
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            mAdapter.remove(mAdapter.getItem(position));
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
                AlertDialog.Builder builder = new AlertDialog.Builder(BuildPrimitiveActivity.this);
                builder.setTitle(R.string.dialogue_title_point);

                LayoutInflater inflater = BuildPrimitiveActivity.this.getLayoutInflater();
                View dialogueView = inflater.inflate(R.layout.dialogue_point_edit, null);

                final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
                final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
                final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);
                final Float3 thisPoint = mAdapter.getItem(position);

                builder.setView(dialogueView);
                builder.setPositiveButton(R.string.dialogue_button_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        float x = (editX.getText().toString().length() == 0) ? 0 : Float.valueOf(editX.getText().toString());
                        float y = (editY.getText().toString().length() == 0) ? 0 : Float.valueOf(editY.getText().toString());
                        float z = (editZ.getText().toString().length() == 0) ? 0 : Float.valueOf(editZ.getText().toString());
                        mAdapter.remove(thisPoint);
                        mAdapter.insert(new Float3(x, y, z), position);
                        mAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.dialogue_button_cancel, new DialogInterface.OnClickListener() {
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
        final View swatch = findViewById(R.id.element_colour_swatch);
        swatch.setBackgroundColor(mElementColour.toArgb());
        buttonColour.setOnClickListener(new EditColourHandler(this, mElementColour, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mElementColour = colour;
                swatch.setBackgroundColor(colour.toArgb());
            }

        }));
    }

    @Override
    public void onBackPressed() {
        saveAndQuit(false);
    }

    protected void saveAndQuit(boolean delete) {
        Intent result = new Intent();
        result.putExtra("delete", delete);

        if (!delete) {
            Type type = (Type) mTypeSpinner.getSelectedItem();
            LinkedList<Float3> vertices = new LinkedList<Float3>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                vertices.add(mAdapter.getItem(i));
            }
            result.putExtra("element", new Primitive(type, vertices, mElementColour));
        }

        setResult(RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
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
}
