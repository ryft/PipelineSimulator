package uk.co.ryft.pipeline.ui.setup;

import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.SetupActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SetupBlendingActivity extends Activity {

    // Store references to view elements
    protected GLConfigSpinner mSpinnerBlendFuncSrc;
    protected GLConfigSpinner mSpinnerBlendFuncDst;
    protected GLConfigSpinner mSpinnerBlendEquation;

    protected Button mButtonSave;
    protected Button mButtonDiscard;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_blending);
        
        // Get camera state from saved instance or bundle, if possible
        Bundle extras = getIntent().getExtras();
        int blendFuncSrc = 1;
        int blendFuncDst = 0;
        int blendEquation = 0;
        
        if (savedInstanceState != null) {
            blendFuncSrc = savedInstanceState.getInt("function_src", blendFuncSrc);
            blendFuncDst = savedInstanceState.getInt("function_dst", blendFuncDst);
            blendEquation = savedInstanceState.getInt("equation", blendEquation);

        } else if (extras != null) {
            blendFuncSrc = extras.getInt("function_src", blendFuncSrc);
            blendFuncDst = extras.getInt("function_dst", blendFuncDst);
            blendEquation = extras.getInt("equation", blendEquation);
        }

        // Set current blending parameters as default selections
        mSpinnerBlendFuncSrc = (GLConfigSpinner) findViewById(R.id.spinner_blendfunc_src);
        GLConfigAdapter<Integer> srcAdapter = new GLConfigAdapter<Integer>(this, android.R.layout.simple_list_item_1,
                SetupActivity.BlendFunc);
        mSpinnerBlendFuncSrc.setAdapter(srcAdapter);
        mSpinnerBlendFuncSrc.setSelection(blendFuncSrc);

        mSpinnerBlendFuncDst = (GLConfigSpinner) findViewById(R.id.spinner_blendfunc_dst);
        GLConfigAdapter<Integer> dstAdapter = new GLConfigAdapter<Integer>(this, android.R.layout.simple_list_item_1,
                SetupActivity.BlendFunc);
        mSpinnerBlendFuncDst.setAdapter(dstAdapter);
        mSpinnerBlendFuncDst.setSelection(blendFuncDst);

        mSpinnerBlendEquation = (GLConfigSpinner) findViewById(R.id.spinner_blendequation);
        GLConfigAdapter<Integer> eqnAdapter = new GLConfigAdapter<Integer>(this, android.R.layout.simple_list_item_1,
                SetupActivity.BlendEquation);
        mSpinnerBlendEquation.setAdapter(eqnAdapter);
        mSpinnerBlendEquation.setSelection(blendEquation);

        mButtonSave = (Button) findViewById(R.id.button_row_positive);
        mButtonDiscard = (Button) findViewById(R.id.button_row_negative);
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

        // Update view components
    }

    @Override
    public void onBackPressed() {
        saveAndQuit();
    }

    protected void saveAndQuit() {
        Intent result = new Intent();
        result.putExtra("function_src", mSpinnerBlendFuncSrc.getSelectedItemPosition());
        result.putExtra("function_dst", mSpinnerBlendFuncDst.getSelectedItemPosition());
        result.putExtra("equation", mSpinnerBlendEquation.getSelectedItemPosition());
        
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void discardAndQuit() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

}
