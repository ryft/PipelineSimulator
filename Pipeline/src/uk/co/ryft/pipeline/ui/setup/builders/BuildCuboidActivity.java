package uk.co.ryft.pipeline.ui.setup.builders;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import uk.co.ryft.pipeline.ui.components.EditColourHandler;
import uk.co.ryft.pipeline.ui.components.EditColourHandler.OnColourChangedListener;
import uk.co.ryft.pipeline.ui.components.EditPointHandler;
import uk.co.ryft.pipeline.ui.components.EditPointHandler.OnPointChangedListener;
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

public class BuildCuboidActivity extends Activity {

    protected Float3 mPointCentre = new Float3(0, 0, 0);

    protected Colour mColourFront = Colour.BLUE;
    protected Colour mColourSide = Colour.GREEN;

    // Store references to view elements
    protected TextView mTextCentre;

    protected View mSwatchFront;
    protected View mSwatchSide;
    protected ImageButton mButtonFront;
    protected ImageButton mButtonSide;

    protected EditText mTextWidth;
    protected EditText mTextHeight;
    protected EditText mTextDepth;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_cuboid);

        // Find interactive components
        mTextCentre = (TextView) findViewById(R.id.text_point);

        mSwatchFront = (View) findViewById(R.id.element_frontcolour_swatch);
        mSwatchSide = (View) findViewById(R.id.element_sidecolour_swatch);
        mButtonFront = (ImageButton) findViewById(R.id.button_element_frontcolour);
        mButtonSide = (ImageButton) findViewById(R.id.button_element_sidecolour);

        mTextWidth = (EditText) findViewById(R.id.edit_element_width);
        mTextHeight = (EditText) findViewById(R.id.edit_element_height);
        mTextDepth = (EditText) findViewById(R.id.edit_element_depth);

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

        mButtonFront.setOnClickListener(new EditColourHandler(this, mColourFront, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourFront = colour;
                mSwatchFront.setBackgroundColor(mColourFront.toArgb());
            }

        }));

        mButtonSide.setOnClickListener(new EditColourHandler(this, mColourSide, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourSide = colour;
                mSwatchSide.setBackgroundColor(mColourSide.toArgb());
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
        mTextWidth.setText("0.5");
        mTextHeight.setText("0.5");
        mTextDepth.setText("0.5");

        // Update view components
        mTextCentre.setText(mPointCentre.toString());
        mSwatchFront.setBackgroundColor(mColourFront.toArgb());
        mSwatchSide.setBackgroundColor(mColourSide.toArgb());
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        // Build element from view components
        float width = (mTextWidth.getText().toString().length() == 0) ? 0 : Float.valueOf(mTextWidth.getText().toString());
        float height = (mTextHeight.getText().toString().length() == 0) ? 0 : Float.valueOf(mTextHeight.getText().toString());
        float depth = (mTextDepth.getText().toString().length() == 0) ? 0 : Float.valueOf(mTextDepth.getText().toString());
        result.putExtra("element", ShapeFactory.buildCuboid(mPointCentre, width, height, depth, mColourFront, mColourSide));
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}
