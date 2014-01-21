package uk.co.ryft.pipeline.ui.setup;

import uk.co.ryft.pipeline.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

public class SetupCullingActivity extends Activity {

    // Store references to view elements
    protected CheckBox mCheckBoxEnable;
    protected RadioButton mRadioButtonCW;
    protected RadioButton mRadioButtonCCW;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_culling);

        // Find interactive components
        mCheckBoxEnable = (CheckBox) findViewById(R.id.checkbox_culling_enable);
        mRadioButtonCW = (RadioButton) findViewById(R.id.radio_cw);
        mRadioButtonCCW = (RadioButton) findViewById(R.id.radio_ccw);

        mButtonSave = (Button) findViewById(R.id.button_element_save);
        mButtonDiscard = (Button) findViewById(R.id.button_element_discard);
        mButtonDiscard.setText(R.string.action_button_cancel);

        // Set up listeners
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

        // Set up defaults
        boolean enabled = true;
        boolean clockwise = false;

        // Get elements from returning activity intent or saved state, if possible
        Bundle extras = getIntent().getExtras();

        if (savedInstanceState != null && savedInstanceState.containsKey("enabled"))
            enabled = savedInstanceState.getBoolean("enabled");
        else if (extras != null && extras.containsKey("enabled"))
            enabled = extras.getBoolean("enabled");

        if (savedInstanceState != null && savedInstanceState.containsKey("clockwise"))
            clockwise = savedInstanceState.getBoolean("clockwise");
        else if (extras != null && extras.containsKey("clockwise"))
            clockwise = extras.getBoolean("clockwise");

        mCheckBoxEnable.setChecked(enabled);
        mRadioButtonCW.setChecked(clockwise);

    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        result.putExtra("enabled", mCheckBoxEnable.isChecked());
        result.putExtra("clockwise", mRadioButtonCW.isChecked());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
