
package uk.co.ryft.pipeline.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.FloatPoint;
import uk.co.ryft.pipeline.model.Element;
import uk.co.ryft.pipeline.model.Element.Type;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

public class ElementActivity extends Activity {

    protected boolean isEditMode;
    protected Element mElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle fromScene = this.getIntent().getExtras();
        isEditMode = fromScene.getBoolean("edit_mode");

        if (isEditMode) {
            setTitle("Edit Element");
            mElement = (Element) fromScene.getSerializable("element");

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
            setTitle("Add Element");

            List<FloatPoint> verts = new LinkedList<FloatPoint>();
            verts.add(new FloatPoint(0f, 0.5f, 0f));
            verts.add(new FloatPoint(-0.5f, 0f, 0f));
            verts.add(new FloatPoint(0.5f, -0.5f, 0f));
            mElement = new Element((Type) Type.GL_QUAD_STRIP, verts, Colour.CYAN);

            setContentView(R.layout.activity_element_add);

        }

        final TypeSpinner typeSpinner = (TypeSpinner) findViewById(R.id.element_type_spinner);
        TypeSpinnerAdapter mTypeAdapter = new TypeSpinnerAdapter(this,
                android.R.layout.simple_list_item_1, Element.Type.values());
        typeSpinner.setAdapter(mTypeAdapter);

        ListView pointList = (ListView) findViewById(R.id.element_points_list);
        pointList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        final PointAdapter pAdapter = new PointAdapter(this, mElement.getVertices());
        pointList.setAdapter(pAdapter);

        final Button button_save = (Button) findViewById(R.id.button_element_save);
        button_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent result = new Intent();

                mElement.setType((Type) typeSpinner.getSelectedItem());
                mElement.setVertices(pAdapter.mPoints);
                result.putExtra("element", mElement);

                setResult(RESULT_OK, result);
                finish();
            }
        });

        setupActionBar();
    }

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
                finish();
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

    class PointAdapter extends BaseAdapter {

        final Context mContext;
        final ArrayList<FloatPoint> mPoints;
        final LayoutInflater mInflater;
        ImageButton mUpdateButton;
        ImageButton mDeleteButton;

        public PointAdapter(Context context, Collection<FloatPoint> points) {
            super();

            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mPoints = new ArrayList<FloatPoint>();
            mPoints.addAll(points);
        }

        public boolean add(FloatPoint point) {
            boolean ret = mPoints.add(point);
            notifyDataSetChanged();
            return ret;
        }

        public boolean addAll(Collection<FloatPoint> points) {
            boolean ret = mPoints.addAll(points);
            notifyDataSetChanged();
            return ret;
        }

        public FloatPoint remove(int index) {
            FloatPoint ret = mPoints.remove(index);
            notifyDataSetChanged();
            return ret;
        }

        public boolean removeAll(Collection<FloatPoint> points) {
            boolean ret = mPoints.removeAll(points);
            notifyDataSetChanged();
            return ret;
        }

        @SuppressWarnings("unchecked")
        public ArrayList<Element> getAllElements() {
            return (ArrayList<Element>) mPoints.clone();
        }

        public void clear() {
            mPoints.clear();
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // Recycle view if possible
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_point, null);
            }

//            final EditText pointX = (EditText) convertView.findViewById(R.id.textinput_point_x);
//            final EditText pointY = (EditText) convertView.findViewById(R.id.textinput_point_y);
//            final EditText pointZ = (EditText) convertView.findViewById(R.id.textinput_point_z);
//
//            pointX.setText(String.valueOf(mElement.getVertices().get(position).getX()));
//            pointY.setText(String.valueOf(mElement.getVertices().get(position).getY()));
//            pointZ.setText(String.valueOf(mElement.getVertices().get(position).getZ()));

//            mUpdateButton = (ImageButton) convertView.findViewById(R.id.button_point_update);
//            mUpdateButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // XXX these should be safe casts, see edittext xml
//                    // parameters
//                    mPoints.get(position).setX(Float.valueOf(pointX.getText().toString()));
//                    mPoints.get(position).setY(Float.valueOf(pointY.getText().toString()));
//                    mPoints.get(position).setZ(Float.valueOf(pointZ.getText().toString()));
//                }
//            });

//            mDeleteButton = (ImageButton) convertView.findViewById(R.id.button_point_remove);
//            mDeleteButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    remove(position);
//                    pointX.setText(String.valueOf(mElement.getVertices().get(position).getX()));
//                    pointY.setText(String.valueOf(mElement.getVertices().get(position).getY()));
//                    pointZ.setText(String.valueOf(mElement.getVertices().get(position).getZ()));
//                }
//            });

            return convertView;
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
    }

}
