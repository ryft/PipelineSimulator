package uk.co.ryft.pipeline.ui.setup.builders;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import uk.co.ryft.pipeline.ui.components.EditColourHandler;
import uk.co.ryft.pipeline.ui.components.EditPointHandler;
import uk.co.ryft.pipeline.ui.components.OnColourChangedListener;
import uk.co.ryft.pipeline.ui.components.OnPointChangedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class BuildCylinderActivity extends Activity {

    protected Float3 mPointCentre = new Float3(0, 0, 0);

    protected Colour mColourCap = Colour.BLUE;
    protected Colour mColourBody = Colour.GREEN;

    // Store references to view elements
    protected TextView mTextCentre;

    protected View mSwatchCap;
    protected View mSwatchBody;
    protected ImageButton mButtonCap;
    protected ImageButton mButtonBody;

    protected EditText mTextHeight;
    protected EditText mTextRadius;
    protected EditText mTextRotation;
    protected EditText mTextStepCount;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_cylinder);

        // Find interactive components
        mTextCentre = (TextView) findViewById(R.id.text_point);

        mSwatchCap = (View) findViewById(R.id.element_capcolour_swatch);
        mSwatchBody = (View) findViewById(R.id.element_bodycolour_swatch);
        mButtonCap = (ImageButton) findViewById(R.id.button_element_capcolour);
        mButtonBody = (ImageButton) findViewById(R.id.button_element_bodycolour);

        mTextHeight = (EditText) findViewById(R.id.edit_element_height);
        mTextRadius = (EditText) findViewById(R.id.edit_element_radius);
        mTextRotation = (EditText) findViewById(R.id.edit_element_rotation);
        mTextStepCount = (EditText) findViewById(R.id.edit_element_stepcount);

        mButtonSave = (Button) findViewById(R.id.button_row_positive);
        mButtonDiscard = (Button) findViewById(R.id.button_row_negative);

        // Set up listeners
        mTextCentre.setGravity(Gravity.RIGHT);
        mTextCentre.setOnClickListener(new EditPointHandler(this, mPointCentre, new OnPointChangedListener() {

            @Override
            public void notifyPointChanged(Float3 point) {
                mPointCentre = point;
                mTextCentre.setText(mPointCentre.toString());
            }

        }));

        mButtonCap.setOnClickListener(new EditColourHandler(this, mColourCap, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourCap = colour;
                mSwatchCap.setBackgroundColor(mColourCap.toArgb());
            }

        }));

        mButtonBody.setOnClickListener(new EditColourHandler(this, mColourBody, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourBody = colour;
                mSwatchBody.setBackgroundColor(mColourBody.toArgb());
            }

        }));

        mButtonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveAndQuit();
            }

        });
        mButtonDiscard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                discardAndQuit();
            }
        });

        // Initialise sensible defaults
        mTextHeight.setText("0.5");
        mTextRadius.setText("0.5");
        mTextRotation.setText("0");
        mTextStepCount.setText("16");

        // Update view components
        mTextCentre.setText(mPointCentre.toString());
        mSwatchCap.setBackgroundColor(mColourCap.toArgb());
        mSwatchBody.setBackgroundColor(mColourBody.toArgb());
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        // Build element from view components
        int stepCount = Integer.valueOf(mTextStepCount.getText().toString());
        float height = Float.valueOf(mTextHeight.getText().toString());
        float radius = Float.valueOf(mTextRadius.getText().toString());
        float rotation = Float.valueOf(mTextRotation.getText().toString());
        result.putExtra("element", ShapeFactory.buildCylinder(stepCount, mPointCentre, height, radius, rotation, mColourBody, mColourCap));
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
