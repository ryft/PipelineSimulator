package uk.co.ryft.pipeline.ui.builders;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class CameraActivity extends Activity {

    protected Float3 mPointEye = new Float3(0, 0, 0);

    protected Colour mColourBody = Colour.GREY;
    protected Colour mColourLens = Colour.WHITE;
    protected Colour mColourShutter = Colour.BLACK;

    // Store references to view elements
    protected TextView mTextEye;

    protected View mSwatchBody;
    protected View mSwatchLens;
    protected View mSwatchShutter;
    protected ImageButton mButtonBody;
    protected ImageButton mButtonLens;
    protected ImageButton mButtonShutter;

    protected EditText mTextScale;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_camera);

        // Find interactive components
        mTextEye = (TextView) findViewById(R.id.text_point);

        mSwatchBody = (View) findViewById(R.id.element_bodycolour_swatch);
        mSwatchLens = (View) findViewById(R.id.element_lenscolour_swatch);
        mSwatchShutter = (View) findViewById(R.id.element_shuttercolour_swatch);
        mButtonBody = (ImageButton) findViewById(R.id.button_element_bodycolour);
        mButtonLens = (ImageButton) findViewById(R.id.button_element_lenscolour);
        mButtonShutter = (ImageButton) findViewById(R.id.button_element_shuttercolour);

        mTextScale = (EditText) findViewById(R.id.edit_element_scale);

        mButtonSave = (Button) findViewById(R.id.button_element_save);
        mButtonDiscard = (Button) findViewById(R.id.button_element_discard);

        // Set up listeners
        mTextEye.setOnClickListener(new EditPointHandler(this, mPointEye, new OnPointChangedListener() {

            @Override
            public void notifyPointChanged(Float3 point) {
                mPointEye = point;
                mTextEye.setText(mPointEye.toString());
            }

        }));

        mButtonBody.setOnClickListener(new EditColourHandler(this, mColourBody, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourBody = colour;
                mSwatchBody.setBackgroundColor(mColourBody.toArgb());
            }

        }));

        mButtonLens.setOnClickListener(new EditColourHandler(this, mColourLens, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourLens = colour;
                mSwatchLens.setBackgroundColor(mColourLens.toArgb());
            }

        }));

        mButtonShutter.setOnClickListener(new EditColourHandler(this, mColourShutter, new OnColourChangedListener() {

            @Override
            public void notifyColourChanged(Colour colour) {
                mColourShutter = colour;
                mSwatchShutter.setBackgroundColor(mColourShutter.toArgb());
            }

        }));

        mButtonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                // Build element from view components
                float scale = Float.valueOf(mTextScale.getText().toString());

                result.putExtra("element",
                        ShapeFactory.buildCamera(scale, mColourBody, mColourLens, mColourShutter).translate(mPointEye));
                setResult(Activity.RESULT_OK, result);
                finish();
            }

        });
        mButtonDiscard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // Initialise sensible defaults
        mTextScale.setText("0.25");

        // Update view components
        mTextEye.setText(mPointEye.toString());
        mSwatchBody.setBackgroundColor(mColourBody.toArgb());
        mSwatchLens.setBackgroundColor(mColourLens.toArgb());
        mSwatchShutter.setBackgroundColor(mColourShutter.toArgb());
    }

}
