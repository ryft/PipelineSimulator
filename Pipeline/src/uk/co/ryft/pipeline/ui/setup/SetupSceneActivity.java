package uk.co.ryft.pipeline.ui.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.shapes.Composite;
import uk.co.ryft.pipeline.model.shapes.Composite.Type;
import uk.co.ryft.pipeline.model.shapes.ElementType;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.ui.components.EditPointHandler;
import uk.co.ryft.pipeline.ui.components.EditPointHandler.OnPointChangedListener;
import uk.co.ryft.pipeline.ui.setup.builders.BuildPrimitiveActivity;
import android.app.Activity;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseView.ConfigOptions;
import com.espian.showcaseview.targets.Target;
import com.espian.showcaseview.targets.ViewTarget;
import com.example.android.swipedismiss.SwipeDismissListViewTouchListener;

public class SetupSceneActivity extends ListActivity {

    // Request values
    protected static final int REQUEST_PRIMITIVE_ADD = 2;
    protected static final int REQUEST_PRIMITIVE_EDIT = 3;
    protected static final int REQUEST_COMPOSITE_ADD = 4;

    protected ElementAdapter mAdapter;

    // XXX Need to store this reference to be able to update/delete.
    // If we pass the worked-on element back and forth, it gets serialised and
    // we lose the reference.
    protected Primitive mThisElement;

    protected Button mSaveButton;
    protected boolean mSelectionMode = false;
    protected final Set<Integer> mSelectedIDs = Collections.synchronizedSet(new HashSet<Integer>());

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_scene);

        // Get elements from returning activity intent or saved state, if possible.
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
        SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return !mSelectionMode;
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

        // Set up save / delete button listeners
        mSaveButton = (Button) findViewById(R.id.button_row_positive);
        Button deleteButton = (Button) findViewById(R.id.button_row_negative);
        deleteButton.setText(R.string.action_button_cancel);

        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectionMode)
                    saveSelectedComposite();
                else
                    saveAndQuit();
            }
        });

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectionMode) {
                    mSelectionMode = false;
                    mSaveButton.setText("Save");
                    mAdapter.notifyDataSetChanged();
                } else
                    discardAndQuit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveSelectedComposite() {

        if (mSelectedIDs.size() > 1) {
            LinkedList<Element> components = new LinkedList<Element>();
            for (int position : mSelectedIDs) {
                components.add((Element) mAdapter.getItem(position));
            }

            mAdapter.removeAll(components);
            mAdapter.add(new Composite(Type.CUSTOM, components));

            Toast.makeText(SetupSceneActivity.this, "Created composite element of size " + mSelectedIDs.size(),
                    Toast.LENGTH_SHORT).show();
        }

        mSelectionMode = false;
        mSaveButton.setText("Save");
        mAdapter.notifyDataSetChanged();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        result.putExtra("elements", mAdapter.getAllElements());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
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
        if (mSelectionMode)
            return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.action_primitive_help:
                ShowcaseView sv = ShowcaseView.insertShowcaseView(new ViewTarget(mSaveButton), this, R.string.help_scene_title,
                        R.string.help_scene_desc, null);
                sv.setScaleMultiplier(0);
                break;

            case R.id.action_primitive_new:
                addPrimitive();
                return true;

            case R.id.action_composite_new:
                addComposite();
                return true;

            case R.id.action_elements_discard:
                mAdapter.clear();
                Toast.makeText(this, R.string.message_scene_clear, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ShowcaseView insertShowcaseView(View target, int title, int description, float scale, ConfigOptions options,
            OnShowcaseEventListener listener) {
        return insertShowcaseView(new ViewTarget(target), title, description, scale, options, listener);
    }

    protected ShowcaseView insertShowcaseView(Target target, int title, int description, float scale, ConfigOptions options,
            OnShowcaseEventListener listener) {
        ShowcaseView sv = ShowcaseView.insertShowcaseView(target, this, title, description, options);
        sv.setOnShowcaseEventListener(listener);
        sv.setScaleMultiplier(scale);
        return sv;
    }

    protected void addPrimitive() {
        Intent intent = new Intent(this, BuildPrimitiveActivity.class);
        intent.putExtra("edit_mode", false);
        startActivityForResult(intent, REQUEST_PRIMITIVE_ADD);
    }

    protected void editPrimitive(Primitive e) {
        Intent intent = new Intent(this, BuildPrimitiveActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("element", e);
        mThisElement = e;
        startActivityForResult(intent, REQUEST_PRIMITIVE_EDIT);
    }

    protected void addComposite() {

        // Collect composite type descriptions into an array
        final ElementType[] types = Composite.Type.values();
        String[] typeNames = new String[types.length];
        for (int i = 0; i < types.length; i++)
            typeNames[i] = types[i].getDescription();

        // Instantiate and display a float picker dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialogue_title_composite_new);

        builder.setItems(typeNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ElementType type = types[which];

                if (type == Composite.Type.CUSTOM) {
                    mSelectedIDs.clear();
                    mSelectionMode = true;
                    mAdapter.notifyDataSetChanged();
                    mSaveButton.setText("Finish");

                } else
                    startActivityForResult(new Intent(SetupSceneActivity.this, type.getEditorActivity()), REQUEST_COMPOSITE_ADD);
            }
        });

        // Get the AlertDialog, initialise values and show it.
        AlertDialog dialogue = builder.create();
        dialogue.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String message = "";

        // Parameter key:
        //
        // For adding a new primitive (REQUEST_PRIMITIVE_ADD)
        // OK result -> new element is 'element' extra
        // Otherwise -> take no action
        //
        // For editing a primitive (REQUEST_PRIMITIVE_EDIT)
        // OK result -> new element is 'element' extra
        // >> If 'deleted' is set true, remove old element
        // >> Otherwise replace old element with new
        // Otherwise -> take no action
        //
        // For adding a composite (REQUEST_COMPOSITE_ADD)
        // OK result -> new element is 'element' extra
        // Otherwise -> take no action

        // If the request was REQUEST_PRIMITIVE_ADD
        if (requestCode == REQUEST_PRIMITIVE_ADD) {

            if (resultCode == Activity.RESULT_OK) {
                if (!data.getBooleanExtra("delete", false)) {
                    mAdapter.add((Primitive) data.getSerializableExtra("element"));
                    message = getString(R.string.message_element_added);

                } else
                    message = getString(R.string.message_element_discarded);
            }

            // If the request was REQUEST_PRIMITIVE_EDIT
        } else if (requestCode == REQUEST_PRIMITIVE_EDIT) {

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
                // FIXME Not necessarily, if user has pressed delete

            } else {
                mThisElement = null;
                message = getString(R.string.message_element_deleted);
            }

        } else if (requestCode == REQUEST_COMPOSITE_ADD) {

            if (resultCode == Activity.RESULT_OK) {
                mAdapter.add((Composite) data.getSerializableExtra("element"));
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

    // View holder stores references to the view components
    static class ElementViewHolder {
        ImageView elemIcon;
        TextView typeTextView;
        ImageButton transformButton;
        ImageButton editButton;
        TextView summaryTextView;
        CheckBox selectionCheckBox;
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

        public boolean add(Element element) {
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

            ElementViewHolder viewHolder;

            // Recycle view if possible
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_element, null);

                // Implement the View Holder pattern
                viewHolder = new ElementViewHolder();
                viewHolder.elemIcon = (ImageView) convertView.findViewById(R.id.element_icon);
                viewHolder.typeTextView = (TextView) convertView.findViewById(R.id.element_type);
                viewHolder.transformButton = (ImageButton) convertView.findViewById(R.id.button_element_transform);
                viewHolder.editButton = (ImageButton) convertView.findViewById(R.id.button_element_edit);
                viewHolder.summaryTextView = (TextView) convertView.findViewById(R.id.element_summary);
                viewHolder.selectionCheckBox = (CheckBox) convertView.findViewById(R.id.element_checkbox);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ElementViewHolder) convertView.getTag();
            }

            ImageView elemIcon = viewHolder.elemIcon;
            TextView typeTextView = viewHolder.typeTextView;
            ImageButton transformButton = viewHolder.transformButton;
            ImageButton editButton = viewHolder.editButton;
            TextView summaryTextView = viewHolder.summaryTextView;
            CheckBox selectionCheckBox = viewHolder.selectionCheckBox;

            Element elem = mElems.get(position);

            if (elem != null) {

                final boolean isPrimitive = elem.isPrimitive();

                elemIcon.setImageResource(elem.getIconRef());
                typeTextView.setText(elem.getTitle());
                summaryTextView.setText(elem.getSummary());
                typeTextView.setClickable(false);

                if (mSelectionMode) {
                    transformButton.setVisibility(View.INVISIBLE);
                    editButton.setVisibility(View.INVISIBLE);
                    selectionCheckBox.setVisibility(View.VISIBLE);

                    // Remove previous listener so we don't accidentally change state
                    selectionCheckBox.setOnCheckedChangeListener(null);
                    selectionCheckBox.setChecked(mSelectedIDs.contains(position));
                    selectionCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                mSelectedIDs.add(position);
                            else
                                mSelectedIDs.remove(position);
                            notifyDataSetChanged();
                        }
                    });

                } else {
                    transformButton.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.VISIBLE);
                    selectionCheckBox.setVisibility(View.INVISIBLE);

                    if (isPrimitive)
                        editButton.setImageResource(R.drawable.ic_action_edit);
                    else
                        editButton.setImageResource(R.drawable.ic_action_expand);
                }

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
                            Toast.makeText(SetupSceneActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final EditPointHandler translationHandler = new EditPointHandler(SetupSceneActivity.this, new Float3(0, 0, 0),
                        R.string.dialogue_title_elem_translation, new OnPointChangedListener() {
                            @Override
                            public void notifyPointChanged(Float3 point) {

                                Element elem = (Element) getItem(position);
                                remove(elem);
                                add(elem.translate(point));
                            }
                        });

                final View thisView = convertView;
                transformButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        // Instantiate and display a configuration dialogue
                        AlertDialog.Builder builder = new AlertDialog.Builder(SetupSceneActivity.this);
                        builder.setTitle(R.string.dialogue_title_select_transformation);
                        builder.setItems(new CharSequence[] { "Apply translation", "Apply rotation" },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0)
                                            translationHandler.onClick(thisView);

                                        else if (which == 1) {

                                            // Instantiate and display a rotation configuration dialogue
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SetupSceneActivity.this);
                                            builder.setTitle(R.string.dialogue_title_elem_rotation);

                                            LayoutInflater inflater = SetupSceneActivity.this.getLayoutInflater();
                                            View dialogueView = inflater.inflate(R.layout.dialogue_rotation, null);

                                            final EditText editAngle = (EditText) dialogueView.findViewById(R.id.edit_angle);
                                            final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
                                            final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
                                            final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);

                                            builder.setView(dialogueView);
                                            builder.setPositiveButton(R.string.dialogue_button_save,
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            float x = Float.valueOf(editX.getText().toString());
                                                            float y = Float.valueOf(editY.getText().toString());
                                                            float z = Float.valueOf(editZ.getText().toString());

                                                            Element elem = (Element) getItem(position);
                                                            Float angle = Float.valueOf(editAngle.getText().toString());
                                                            remove(elem);
                                                            add(elem.rotate(angle, x, y, z));
                                                        }
                                                    });
                                            builder.setNegativeButton(R.string.dialogue_button_cancel, null);

                                            // Get the AlertDialog, initialise values and show it.
                                            AlertDialog dialogue = builder.create();

                                            Float3 initialPoint = new Float3(0, 0, 0);
                                            editX.setText(String.valueOf(initialPoint.getX()));
                                            editY.setText(String.valueOf(initialPoint.getY()));
                                            editZ.setText(String.valueOf(initialPoint.getZ()));
                                            dialogue.show();
                                        }
                                        notifyDataSetChanged();
                                    }
                                });
                        AlertDialog dialogue = builder.create();
                        dialogue.show();
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
