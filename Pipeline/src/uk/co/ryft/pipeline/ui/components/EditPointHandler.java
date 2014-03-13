package uk.co.ryft.pipeline.ui.components;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Float3;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class EditPointHandler implements OnClickListener {

    protected Activity mParent;
    protected Float3 mPoint;
    protected OnPointChangedListener mListener;

    // TODO this relies on the mutability of Float3s.
    // This is becoming a serious issue which needs to be resolved soon, if ever.
    public EditPointHandler(Activity parent, Float3 point, OnPointChangedListener listener) {
        mParent = parent;
        mPoint = point;
        mListener = listener;
    }

    // Package visibility for ListPointHandler
    void setPoint(Float3 point) {
        mPoint = point;
    }

    @Override
    public void onClick(View v) {

        // Instantiate and display a float picker dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(mParent);
        builder.setTitle(R.string.dialogue_title_point);

        LayoutInflater inflater = mParent.getLayoutInflater();
        View dialogueView = inflater.inflate(R.layout.dialogue_point_edit, null);

        final EditText editX = (EditText) dialogueView.findViewById(R.id.edit_point_x);
        final EditText editY = (EditText) dialogueView.findViewById(R.id.edit_point_y);
        final EditText editZ = (EditText) dialogueView.findViewById(R.id.edit_point_z);

        builder.setView(dialogueView);
        builder.setPositiveButton(R.string.dialogue_button_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                float x = Float.valueOf(editX.getText().toString());
                float y = Float.valueOf(editY.getText().toString());
                float z = Float.valueOf(editZ.getText().toString());
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
