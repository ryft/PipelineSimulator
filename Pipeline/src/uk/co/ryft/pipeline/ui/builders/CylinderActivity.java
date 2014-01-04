package uk.co.ryft.pipeline.ui.builders;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.gl.Colour;
import uk.co.ryft.pipeline.gl.Float3;
import uk.co.ryft.pipeline.model.shapes.ShapeFactory;
import uk.co.ryft.pipeline.ui.components.PointClickListener;
import uk.co.ryft.pipeline.ui.components.PointListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class CylinderActivity extends PointListener {
    
    protected Float3 mPointCentre = new Float3(0, 0, 0);
    
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
    
    protected Colour mColourCap;
    protected Colour mColourBody;
    
    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_cylinder);
        
        // Find interactive components
        mTextCentre = (TextView) findViewById(R.id.text_point);
        
        mSwatchCap = (View) findViewById(R.id.element_capcolour_swatch);
        mSwatchBody = (View) findViewById(R.id.element_bodycolour_swatch);
        mSwatchCap = (ImageButton) findViewById(R.id.button_element_capcolour);
        mSwatchBody = (ImageButton) findViewById(R.id.button_element_bodycolour);

        mTextHeight = (EditText) findViewById(R.id.edit_element_height);
        mTextRadius = (EditText) findViewById(R.id.edit_element_radius);
        mTextRotation = (EditText) findViewById(R.id.edit_element_rotation);
        mTextStepCount = (EditText) findViewById(R.id.edit_element_stepcount);

        mButtonSave = (Button) findViewById(R.id.button_element_save);
        mButtonDiscard = (Button) findViewById(R.id.button_element_discard);
        
        // Set up listeners
        mTextCentre.setOnClickListener(new PointClickListener(this, mPointCentre));
        
        mButtonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
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
            
        });
        
        // Initialise sensible defaults
        mColourCap = Colour.BLUE;
        mColourBody = Colour.CYAN;
        mTextHeight.setText("0.5");
        mTextRadius.setText("0.5");
        mTextRotation.setText("0");
        mTextStepCount.setText("16");
        
        notifyPointChanged();
    }

    @Override
    public void notifyPointChanged() {
        mTextCentre.setText(mPointCentre.toString());
    }

}
