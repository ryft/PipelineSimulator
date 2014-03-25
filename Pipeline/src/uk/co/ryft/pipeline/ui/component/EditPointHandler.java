package uk.co.ryft.pipeline.ui.component;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.Float3;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class EditPointHandler implements OnClickListener {

    protected final Activity mParent;
    protected Float3 mPoint;
    protected final int mTitle;
    protected final OnPointChangedListener mListener;

    public EditPointHandler(Activity parent, Float3 point, OnPointChangedListener listener) {
        this(parent, point, R.string.dialogue_title_point, listener);
    }

    public EditPointHandler(Activity parent, Float3 point, int title, OnPointChangedListener listener) {
        mParent = parent;
        mPoint = point;
        mTitle = title;
        mListener = listener;
    }

    @Override
    public void onClick(View v) {

        // Instantiate and display a float picker dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(mParent);
        builder.setTitle(mTitle);

        LayoutInflater inflater = mParent.getLayoutInflater();
        View dialogueView = inflater.inflate(R.layout.dialogue_point_edit, null);

        final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
        final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
        final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);

        builder.setView(dialogueView);
        builder.setPositiveButton(R.string.dialogue_button_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                float x = (editX.getText().toString().length() == 0) ? 0 : Float.valueOf(editX.getText().toString());
                float y = (editY.getText().toString().length() == 0) ? 0 : Float.valueOf(editY.getText().toString());
                float z = (editZ.getText().toString().length() == 0) ? 0 : Float.valueOf(editZ.getText().toString());
                mPoint = new Float3(x, y, z);
                mListener.notifyPointChanged(mPoint);
            }
        });
        builder.setNegativeButton(R.string.dialogue_button_cancel, null);

        // Get the AlertDialog, initialise values and show it.
        AlertDialog dialogue = builder.create();

        editX.setText(String.valueOf(mPoint.getX()));
        editY.setText(String.valueOf(mPoint.getY()));
        editZ.setText(String.valueOf(mPoint.getZ()));
        dialogue.show();
    }

    public interface OnPointChangedListener {

        public void notifyPointChanged(Float3 point);

    }
}
