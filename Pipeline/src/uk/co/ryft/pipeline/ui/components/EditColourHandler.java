package uk.co.ryft.pipeline.ui.components;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.settings.notificationlight.ColorPickerView;

public class EditColourHandler implements OnClickListener {
    
    protected Activity mParent;
    protected Colour mColour;
    protected OnColourChangedListener mListener;
    
    public EditColourHandler(Activity parent, Colour colour, OnColourChangedListener listener) {
        mParent = parent;
        mColour = colour;
        
        // We don't want to use their OnColorChangedListener as it's too verbose
        // We only want to send notifications when the dialogue closes.
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        
        // Instantiate and display a Colour Picker dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(mParent);
        builder.setTitle(R.string.dialogue_title_colour);

        final ColorPickerView dialogueView = new ColorPickerView(mParent);

        builder.setView(dialogueView);
        dialogueView.setColor(mColour.toArgb(), false);
        
        builder.setPositiveButton(R.string.dialogue_button_save,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mColour = Colour.fromArgb(dialogueView.getColor());
                        mListener.notifyColourChanged(mColour);
                    }
                });
        builder.setNegativeButton(R.string.dialogue_button_cancel, null);

        // Get the AlertDialog, initialise values and show it.
        AlertDialog dialogue = builder.create();
        dialogue.show();
    }

}
